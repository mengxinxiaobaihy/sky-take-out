//package com.sky.utils;
//
//import com.aliyun.oss.ClientException;
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import java.io.ByteArrayInputStream;
//
//@Data
//@AllArgsConstructor
//@Slf4j
//public class AliOssUtil {
//
//    private String endpoint;
//    private String accessKeyId;
//    private String accessKeySecret;
//    private String bucketName;
//
//    /**
//     * 文件上传
//     *
//     * @param bytes
//     * @param objectName
//     * @return
//     */
//    public String upload(byte[] bytes, String objectName) {
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        try {
//            // 创建PutObject请求。
//            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
//        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
//        } catch (ClientException ce) {
//            System.out.println("Caught an ClientException, which means the client encountered "
//                    + "a serious internal problem while trying to communicate with OSS, "
//                    + "such as not being able to access the network.");
//            System.out.println("Error Message:" + ce.getMessage());
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//
//        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
//        StringBuilder stringBuilder = new StringBuilder("https://");
//        stringBuilder
//                .append(bucketName)
//                .append(".")
//                .append(endpoint)
//                .append("/")
//                .append(objectName);
//
//        log.info("文件上传到:{}", stringBuilder.toString());
//
//        return stringBuilder.toString();
//    }
//}


package com.sky.utils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import io.minio.errors.MinioException;
import io.minio.messages.Prefix;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Data
@Component
@ConfigurationProperties(prefix = "alioss")
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建MinioClient实例
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKeyId, accessKeySecret)
                .build();

        try {
            // 将字节数组转换为输入流
            InputStream inputStream = new ByteArrayInputStream(bytes);
            // 创建PutObject请求并上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, bytes.length, -1) // -1表示不限制流的大小
                            .build()
            );
        } catch (MinioException e) {
            System.out.println("MinIO server error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        //文件访问路径规则 MinIO：http://Endpoint/BucketName/ObjectName
        // 确保endpoint以/结尾
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        // 处理objectName开头的/，如有就删除以免后面又添加一个构成双斜杠
        if (objectName.startsWith("/")) {
            //截取字符串从第2个字符开始到末尾的子字符串（即去掉第一个字符）
            objectName = objectName.substring(1);
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(endpoint)
                .append(bucketName)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", urlBuilder.toString());
        return urlBuilder.toString();
    }
}

