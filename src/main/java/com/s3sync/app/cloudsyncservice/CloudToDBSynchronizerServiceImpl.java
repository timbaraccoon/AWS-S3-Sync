package com.s3sync.app.cloudsyncservice;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.s3sync.app.dao.FileInfoRepository;
import com.s3sync.app.entity.FileInfo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@EnableScheduling
@Service
public class CloudToDBSynchronizerServiceImpl implements CloudToDBSynchronizerService {

    private final FileInfoRepository repository;
    private final AmazonS3 s3client;

    private final String bucketName = "cloudaware-test";
    private final Regions region = Regions.US_EAST_1;
    private Date lastDatabaseUpdate;
    private final AtomicInteger cloudObjectsCounter;


    @Autowired
    public CloudToDBSynchronizerServiceImpl(FileInfoRepository repository) {
        this.repository = repository;
        this.s3client = createS3Client();
        lastDatabaseUpdate = new Date(1);
        cloudObjectsCounter = new AtomicInteger(0);
    }

    private AmazonS3 createS3Client() {
        String accessKey = "acckey here";
        String secretKey = "secrkey here";
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Override
    @Async
    @Scheduled(fixedRate = 3_600_000) // 1 hour
    public void runRepeatingDatabaseHarmonize() {
        synchronizeDataBaseFromCloud();
    }

    public void synchronizeDataBaseFromCloud() {
        saveNewInfoFromCloudToDB();

        if (cloudObjectsCounter.get() != repository.count()) {
            findAndRemoveDeletedData();
        }
    }


    private void saveNewInfoFromCloudToDB() {
        cloudObjectsCounter.set(0);
        if (repository.count() > 0) {
            lastDatabaseUpdate = repository.getMaxLastModified();
        }

        ConcurrentLinkedQueue<FileInfo> newDataToSave = new ConcurrentLinkedQueue<>();
        ListVersionsRequest listRequest =
                new ListVersionsRequest().withBucketName(bucketName);
        VersionListing vs;

        do {
            vs = s3client.listVersions(listRequest);
            Queue<S3VersionSummary> versionSummaries =
                    new ConcurrentLinkedQueue<>(vs.getVersionSummaries());
            cloudObjectsCounter.addAndGet(versionSummaries.size());

            downloadNewDataFromS3(newDataToSave, versionSummaries); // bottleneck was here
            saveNewDataToDB(newDataToSave);

            listRequest.setKeyMarker(vs.getNextKeyMarker());

        } while (vs.isTruncated());
    }

    private void downloadNewDataFromS3(ConcurrentLinkedQueue<FileInfo> newDataToSave,
                                       Queue<S3VersionSummary> versionSums) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(20);

        Runnable task = () -> {
            while (!versionSums.isEmpty()) {
                S3VersionSummary summary = versionSums.poll();
                if (summary != null && summary.getLastModified().after(lastDatabaseUpdate)) {
                    newDataToSave.add(
                            createFileInfoFromObjectSummary(summary));
                }
            }
            latch.countDown();
        };

        executeAndCatchThreads(executorService, latch, task);
        executorService.shutdown();
    }

    private void executeAndCatchThreads(ExecutorService executorService,
                                        CountDownLatch latch,
                                        Runnable task) {
        for (int i = 0; i < 20; i++) {
            executorService.execute(task);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveNewDataToDB(ConcurrentLinkedQueue<FileInfo> cloudNewData) {
        repository.saveAll(cloudNewData);
        cloudNewData.clear();
    }

    private void findAndRemoveDeletedData() {
        repository.updateAllToConfirmed(false);

        ListVersionsRequest listRequest =
                new ListVersionsRequest().withBucketName(bucketName);
        VersionListing vs;

        do {
            vs = s3client.listVersions(listRequest);

            Set<String> versionsSet = new HashSet<>();
            for (S3VersionSummary versionSummary : vs.getVersionSummaries()) {
                versionsSet.add(versionSummary.getVersionId());
            }
            repository.updateAllInSetToConfirmed(true, versionsSet);

            listRequest.setKeyMarker(vs.getNextKeyMarker());
        } while (vs.isTruncated());

        long deleted = repository.deleteByConfirmed(false);
        System.out.println("Number of deleted objects: " + deleted);
    }

    private FileInfo createFileInfoFromObjectSummary(S3VersionSummary versionSummary) {
        FileInfo fileInfo = new FileInfo();

        fileInfo.setName(versionSummary.getKey());
        fileInfo.setObjectVersion(versionSummary.getVersionId());
        fileInfo.setType(FilenameUtils.getExtension(versionSummary.getKey()));
        fileInfo.setLastModified(versionSummary.getLastModified());
        fileInfo.setSize(versionSummary.getSize());
        fileInfo.setAclGrants(getObjectAcl(versionSummary.getKey()));
        fileInfo.setStorageClass(versionSummary.getStorageClass());

        return fileInfo;
    }

    private List<Grant> getObjectAcl(String objectKey) {

        List<Grant> grants = new ArrayList<>();

        try {
            AccessControlList acl = s3client.getObjectAcl(bucketName, objectKey);
            grants = acl.getGrantsAsList();

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        return grants;
    }

}
