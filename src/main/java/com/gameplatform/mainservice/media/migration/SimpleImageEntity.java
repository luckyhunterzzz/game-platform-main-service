package com.gameplatform.mainservice.media.migration;

public interface SimpleImageEntity {

    Object getId();

    String getImageBucket();

    String getImageObjectKey();

    void setImageBucket(String bucket);

    void setImageObjectKey(String objectKey);
}
