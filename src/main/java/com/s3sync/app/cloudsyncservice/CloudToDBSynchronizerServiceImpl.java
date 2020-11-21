package com.s3sync.app.cloudsyncservice;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.s3sync.app.dao.FileInfoRepository;
import com.s3sync.app.entity.FileInfo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
public class CloudToDBSynchronizerServiceImpl implements CloudToDBSynchronizerService {

    private final FileInfoRepository repository;
    private final AmazonS3 s3client;

    private long REFRESH_THRESHOLD;
    private final String bucketName = "cloudaware-test"; // "test-ruslan-bucket"; //


    @Autowired
    public CloudToDBSynchronizerServiceImpl(FileInfoRepository repository) {
        this.repository = repository;
        this.s3client = createS3Client();
        REFRESH_THRESHOLD = TimeUnit.MINUTES.toMillis(1);
    }

    private AmazonS3 createS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAZVYIBMMKMH74L2H6",
                "2MmPxx8HSX/UaAVZgJ49apprE2sBf/WMbCZc8z+c"
        );

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1) // Regions.EU_NORTH_1) //
                .build();
    }

    @Override
    public void setSynchronizePeriod(int min) {
        this.REFRESH_THRESHOLD = TimeUnit.MINUTES.toMillis(min);
    }

    @Override
    @PostConstruct
    public void runRepeatingDatabaseHarmonize() {
        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronizeDataBaseFromCloud();

                try {
                    Thread.sleep(REFRESH_THRESHOLD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        };
        new Thread(task).start();
    }

    private void synchronizeDataBaseFromCloud() {
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucketName);

        HashSet<FileInfo> dataBaseSet = new HashSet<>(repository.findAll());
        HashSet<FileInfo> cloudTargetSet = new HashSet<>();

        saveNewInfoFromCloudToDB(listObjectsRequest, dataBaseSet, cloudTargetSet);
        removeIrrelevantFromDB(dataBaseSet, cloudTargetSet);
    }


    private void saveNewInfoFromCloudToDB(ListObjectsRequest listObjectsRequest,
                                          HashSet<FileInfo> dataBaseSet,
                                          HashSet<FileInfo> cloudTargetSet) {
        ObjectListing os;
        do {
            os = s3client.listObjects(listObjectsRequest);

            for (S3ObjectSummary objectSummary : os.getObjectSummaries()) {

                FileInfo fileInfo = createFileInfoFromObjectSummary(objectSummary);
                cloudTargetSet.add(fileInfo);
                if (!(dataBaseSet.contains(fileInfo))) {
                    repository.save(fileInfo);
                }
            }
            listObjectsRequest.setMarker(os.getNextMarker());

        } while (os.isTruncated());
    }


    private void removeIrrelevantFromDB(HashSet<FileInfo> dataBaseSet, HashSet<FileInfo> cloudTargetSet) {
        dataBaseSet.stream().filter(elementInDB -> !(cloudTargetSet.contains(elementInDB)))
                .map(FileInfo::getId)
                .forEach(repository::deleteById);
    }


    private FileInfo createFileInfoFromObjectSummary(S3ObjectSummary objectSummary) {
        FileInfo fileInfo = new FileInfo();

        fileInfo.setName(objectSummary.getKey());
        fileInfo.setType(FilenameUtils.getExtension(objectSummary.getKey()));
        fileInfo.setLastModified(convertToLocalDateTime(objectSummary.getLastModified()));
        fileInfo.setSize(objectSummary.getSize());
        fileInfo.setStorageClass(objectSummary.getStorageClass());

        return fileInfo;
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return new java.sql.Timestamp(
                dateToConvert.getTime()).toLocalDateTime();
    }

}
