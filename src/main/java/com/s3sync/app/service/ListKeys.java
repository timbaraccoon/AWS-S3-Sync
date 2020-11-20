package com.s3sync.app.service;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.util.List;

public class ListKeys {

    public static void main(String[] args) throws IOException {

        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAZVYIBMMKMH74L2H6",
                "2MmPxx8HSX/UaAVZgJ49apprE2sBf/WMbCZc8z+c"
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        AWSS3Service awsService = new AWSS3Service(s3client);


        System.out.println("****  started here *****");

        List<Bucket> buckets = s3client.listBuckets();
        for(Bucket bucket : buckets) {
            System.out.println(bucket.getName());
        }


        String bucketName = "cloudaware-test";


        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                                                        .withBucketName(bucketName);

        ObjectListing os;
        int size = 0;

        // помогает избежать ошибки неполной прогрузки бакета - 1000 max
        do {
            os = s3client.listObjects(listObjectsRequest);
            size += os.getObjectSummaries().size();

            for (S3ObjectSummary objectSummary : os.getObjectSummaries()) {

                System.out.println( " - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() +
                        ")");
            }

            listObjectsRequest.setMarker(os.getNextMarker());
        } while (os.isTruncated());

        System.out.println("\nконтроль");
        System.out.println(size + " <==== кол-во элементов");

        /*ObjectListing objectListing = awsService.listObjects(bucketName);

        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            System.out.println(os.getKey() + " ** " + os.getLastModified() + " ** " + os.getStorageClass());
        }


        System.out.println(objectListing.getObjectSummaries().size() + " <==== кол-во элементов");



        /*
        ObjectListing objectListing = awsService.listObjects(bucketName);

        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            System.out.println(os.getKey() + " ** " + os.getLastModified() + " ** " + os.getStorageClass());
        }


        System.out.println(objectListing.getObjectSummaries().size() + " <==== кол-во элементов");
         */
    }
}