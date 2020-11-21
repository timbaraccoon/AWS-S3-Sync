package com.s3sync.app.cloudsyncservice;

public interface CloudToDBSynchronizerService {

    void runRepeatingHarmonizeDatabase();

    void setSynchronizePeriod(int min);

}
