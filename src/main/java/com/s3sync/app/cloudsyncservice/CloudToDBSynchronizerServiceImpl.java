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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@EnableScheduling
@Service
public class CloudToDBSynchronizerServiceImpl implements CloudToDBSynchronizerService {

    private final FileInfoRepository repository;
    private final AmazonS3 s3client;

    private final String accessKey = "AKIAZVYIBMMKMH74L2H6";
    private final String secretKey = "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";
    private final String bucketName = "test-ruslan-bucket"; // "cloudaware-test"; //
    private final Regions region = Regions.EU_NORTH_1; //Regions.US_EAST_1; //


    @Autowired
    public CloudToDBSynchronizerServiceImpl(FileInfoRepository repository) {
        this.repository = repository;
        this.s3client = createS3Client();
    }

    private AmazonS3 createS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Override
    @PostConstruct
    @Scheduled(fixedRate = 60_000)
    @Async
    public void runRepeatingDatabaseHarmonize() {
        synchronized (this) {
            synchronizeDataBaseFromCloud();
        }
    }

    @Transactional
    public void synchronizeDataBaseFromCloud() {
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucketName);

        Set<FileInfo> dataBaseSet = new HashSet<>(repository.findAll());
        Set<FileInfo> cloudTargetSet = new HashSet<>();

        saveNewInfoFromCloudToDB(listObjectsRequest, dataBaseSet, cloudTargetSet);
        removeIrrelevantFromDB(dataBaseSet, cloudTargetSet);
    }


    private void saveNewInfoFromCloudToDB(ListObjectsRequest listObjectsRequest,
                                          Set<FileInfo> dataBaseSet,
                                          Set<FileInfo> cloudTargetSet) {
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


    private void removeIrrelevantFromDB(Set<FileInfo> dataBaseSet, Set<FileInfo> cloudTargetSet) {
        Set<FileInfo> toDelete = dataBaseSet.stream()
                .filter(elementInDB -> !(cloudTargetSet.contains(elementInDB)))
                .collect(Collectors.toSet());

        repository.deleteAll(toDelete);
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
