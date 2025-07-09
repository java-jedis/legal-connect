package com.javajedis.legalconnect.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

class AwsServiceImplementationTest {

    @Mock
    private AmazonS3 s3Client;

    @InjectMocks
    private AwsServiceImplementation awsService;

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_KEY = "test-file.pdf";
    private static final String TEST_CONTENT_TYPE = "application/pdf";
    private static final Long TEST_CONTENT_LENGTH = 1024L;
    private static final String TEST_CONTENT = "Test file content";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructor() {
        // Arrange & Act
        AwsServiceImplementation service = new AwsServiceImplementation(s3Client);

        // Assert
        assertNotNull(service);
    }

    @Test
    void testUploadFile_Success() throws AmazonClientException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        PutObjectResult putObjectResult = new PutObjectResult();
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(putObjectResult);

        // Act
        String result = awsService.uploadFile(TEST_BUCKET, TEST_KEY, TEST_CONTENT_LENGTH, TEST_CONTENT_TYPE, inputStream);

        // Assert
        assertEquals(TEST_KEY, result);
        verify(s3Client).putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void testUploadFile_WithNullContentLength() throws AmazonClientException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        PutObjectResult putObjectResult = new PutObjectResult();
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(putObjectResult);

        // Act
        String result = awsService.uploadFile(TEST_BUCKET, TEST_KEY, null, TEST_CONTENT_TYPE, inputStream);

        // Assert
        assertEquals(TEST_KEY, result);
        verify(s3Client).putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void testUploadFile_WithNullContentType() throws AmazonClientException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        PutObjectResult putObjectResult = new PutObjectResult();
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(putObjectResult);

        // Act
        String result = awsService.uploadFile(TEST_BUCKET, TEST_KEY, TEST_CONTENT_LENGTH, null, inputStream);

        // Assert
        assertEquals(TEST_KEY, result);
        verify(s3Client).putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void testUploadFile_AmazonClientException() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenThrow(new AmazonClientException("Upload failed"));

        // Act & Assert
        assertThrows(AmazonClientException.class, () -> {
            awsService.uploadFile(TEST_BUCKET, TEST_KEY, TEST_CONTENT_LENGTH, TEST_CONTENT_TYPE, inputStream);
        });
    }

    @Test
    void testDownloadFile_Success() throws IOException, AmazonClientException {
        // Arrange
        S3Object s3Object = mockS3Object();
        when(s3Client.getObject(TEST_BUCKET, TEST_KEY)).thenReturn(s3Object);

        // Act
        ByteArrayOutputStream result = awsService.downloadFile(TEST_BUCKET, TEST_KEY);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CONTENT, result.toString());
        verify(s3Client).getObject(TEST_BUCKET, TEST_KEY);
    }

    @Test
    void testDownloadFile_AmazonClientException() {
        // Arrange
        when(s3Client.getObject(TEST_BUCKET, TEST_KEY)).thenThrow(new AmazonClientException("Download failed"));

        // Act & Assert
        assertThrows(AmazonClientException.class, () -> {
            awsService.downloadFile(TEST_BUCKET, TEST_KEY);
        });
    }

    @Test
    void testDownloadFile_IOException() {
        // Arrange
        S3Object s3Object = mockS3ObjectWithIOException();
        when(s3Client.getObject(TEST_BUCKET, TEST_KEY)).thenReturn(s3Object);

        // Act & Assert
        assertThrows(IOException.class, () -> {
            awsService.downloadFile(TEST_BUCKET, TEST_KEY);
        });
    }

    @Test
    void testListFiles_Success() throws AmazonClientException {
        // Arrange
        ObjectListing objectListing = mockObjectListing();
        ObjectListing emptyListing = mockEmptyObjectListing();
        when(s3Client.listObjects(TEST_BUCKET)).thenReturn(objectListing);
        when(s3Client.listNextBatchOfObjects(objectListing)).thenReturn(emptyListing);

        // Act
        List<String> result = awsService.listFiles(TEST_BUCKET);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(s3Client).listObjects(TEST_BUCKET);
        verify(s3Client).listNextBatchOfObjects(objectListing);
    }

    @Test
    void testListFiles_EmptyBucket() throws AmazonClientException {
        // Arrange
        ObjectListing emptyListing = mockEmptyObjectListing();
        when(s3Client.listObjects(TEST_BUCKET)).thenReturn(emptyListing);

        // Act
        List<String> result = awsService.listFiles(TEST_BUCKET);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(s3Client).listObjects(TEST_BUCKET);
    }

    @Test
    void testListFiles_WithDirectories() throws AmazonClientException {
        // Arrange
        ObjectListing objectListing = mockObjectListingWithDirectories();
        ObjectListing emptyListing = mockEmptyObjectListing();
        when(s3Client.listObjects(TEST_BUCKET)).thenReturn(objectListing);
        when(s3Client.listNextBatchOfObjects(objectListing)).thenReturn(emptyListing);

        // Act
        List<String> result = awsService.listFiles(TEST_BUCKET);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(s3Client).listObjects(TEST_BUCKET);
    }

    @Test
    void testListFiles_AmazonClientException() {
        // Arrange
        when(s3Client.listObjects(TEST_BUCKET)).thenThrow(new AmazonClientException("List failed"));

        // Act & Assert
        assertThrows(AmazonClientException.class, () -> {
            awsService.listFiles(TEST_BUCKET);
        });
    }

    @Test
    void testDeleteFile_Success() throws AmazonClientException {
        // Arrange
        doNothing().when(s3Client).deleteObject(TEST_BUCKET, TEST_KEY);

        // Act
        awsService.deleteFile(TEST_BUCKET, TEST_KEY);

        // Assert
        verify(s3Client).deleteObject(TEST_BUCKET, TEST_KEY);
    }

    @Test
    void testDeleteFile_AmazonClientException() {
        // Arrange
        doThrow(new AmazonClientException("Delete failed")).when(s3Client).deleteObject(TEST_BUCKET, TEST_KEY);

        // Act & Assert
        assertThrows(AmazonClientException.class, () -> {
            awsService.deleteFile(TEST_BUCKET, TEST_KEY);
        });
    }

    @Test
    void testUploadFile_WithLargeContent() throws AmazonClientException {
        // Arrange
        String largeContent = "A".repeat(10000);
        InputStream inputStream = new ByteArrayInputStream(largeContent.getBytes());
        PutObjectResult putObjectResult = new PutObjectResult();
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(putObjectResult);

        // Act
        String result = awsService.uploadFile(TEST_BUCKET, TEST_KEY, (long) largeContent.length(), TEST_CONTENT_TYPE, inputStream);

        // Assert
        assertEquals(TEST_KEY, result);
        verify(s3Client).putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void testDownloadFile_WithLargeContent() throws IOException, AmazonClientException {
        // Arrange
        String largeContent = "B".repeat(10000);
        S3Object s3Object = mockS3ObjectWithContent(largeContent);
        when(s3Client.getObject(TEST_BUCKET, TEST_KEY)).thenReturn(s3Object);

        // Act
        ByteArrayOutputStream result = awsService.downloadFile(TEST_BUCKET, TEST_KEY);

        // Assert
        assertNotNull(result);
        assertEquals(largeContent, result.toString());
        verify(s3Client).getObject(TEST_BUCKET, TEST_KEY);
    }

    @Test
    void testListFiles_MultipleBatches() throws AmazonClientException {
        // Arrange
        ObjectListing firstBatch = mockObjectListing();
        ObjectListing secondBatch = mockSecondBatchObjectListing();
        ObjectListing emptyBatch = mockEmptyObjectListing();
        
        when(s3Client.listObjects(TEST_BUCKET)).thenReturn(firstBatch);
        when(s3Client.listNextBatchOfObjects(firstBatch)).thenReturn(secondBatch);
        when(s3Client.listNextBatchOfObjects(secondBatch)).thenReturn(emptyBatch);

        // Act
        List<String> result = awsService.listFiles(TEST_BUCKET);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        verify(s3Client).listObjects(TEST_BUCKET);
        verify(s3Client).listNextBatchOfObjects(firstBatch);
        verify(s3Client).listNextBatchOfObjects(secondBatch);
    }

    @Test
    void testListFiles_WithOnlyDirectories() throws AmazonClientException {
        // Arrange
        ObjectListing objectListing = mockObjectListingWithOnlyDirectories();
        ObjectListing emptyListing = mockEmptyObjectListing();
        when(s3Client.listObjects(TEST_BUCKET)).thenReturn(objectListing);
        when(s3Client.listNextBatchOfObjects(objectListing)).thenReturn(emptyListing);

        // Act
        List<String> result = awsService.listFiles(TEST_BUCKET);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(s3Client).listObjects(TEST_BUCKET);
    }

    @Test
    void testUploadFile_WithZeroContentLength() throws AmazonClientException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        PutObjectResult putObjectResult = new PutObjectResult();
        when(s3Client.putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(putObjectResult);

        // Act
        String result = awsService.uploadFile(TEST_BUCKET, TEST_KEY, 0L, TEST_CONTENT_TYPE, inputStream);

        // Assert
        assertEquals(TEST_KEY, result);
        verify(s3Client).putObject(eq(TEST_BUCKET), eq(TEST_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void testDownloadFile_WithEmptyContent() throws IOException, AmazonClientException {
        // Arrange
        S3Object s3Object = mockS3ObjectWithContent("");
        when(s3Client.getObject(TEST_BUCKET, TEST_KEY)).thenReturn(s3Object);

        // Act
        ByteArrayOutputStream result = awsService.downloadFile(TEST_BUCKET, TEST_KEY);

        // Assert
        assertNotNull(result);
        assertEquals("", result.toString());
        verify(s3Client).getObject(TEST_BUCKET, TEST_KEY);
    }

    // Helper methods to create mock objects
    private S3Object mockS3Object() {
        S3Object s3Object = new S3Object();
        S3ObjectInputStream inputStream = new S3ObjectInputStream(
            new ByteArrayInputStream(TEST_CONTENT.getBytes()), null);
        s3Object.setObjectContent(inputStream);
        return s3Object;
    }

    private S3Object mockS3ObjectWithContent(String content) {
        S3Object s3Object = new S3Object();
        S3ObjectInputStream inputStream = new S3ObjectInputStream(
            new ByteArrayInputStream(content.getBytes()), null);
        s3Object.setObjectContent(inputStream);
        return s3Object;
    }

    private S3Object mockS3ObjectWithIOException() {
        S3Object s3Object = new S3Object();
        InputStream mockInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };
        S3ObjectInputStream inputStream = new S3ObjectInputStream(mockInputStream, null);
        s3Object.setObjectContent(inputStream);
        return s3Object;
    }

    private ObjectListing mockObjectListing() {
        ObjectListing objectListing = mock(ObjectListing.class);
        List<S3ObjectSummary> summaries = List.of(
            createS3ObjectSummary("file1.pdf"),
            createS3ObjectSummary("file2.txt")
        );
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        return objectListing;
    }

    private ObjectListing mockSecondBatchObjectListing() {
        ObjectListing objectListing = mock(ObjectListing.class);
        List<S3ObjectSummary> summaries = List.of(
            createS3ObjectSummary("file3.doc"),
            createS3ObjectSummary("file4.xlsx")
        );
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        return objectListing;
    }

    private ObjectListing mockObjectListingWithDirectories() {
        ObjectListing objectListing = mock(ObjectListing.class);
        List<S3ObjectSummary> summaries = List.of(
            createS3ObjectSummary("file1.pdf"),
            createS3ObjectSummary("file2.txt"),
            createS3ObjectSummary("directory/")
        );
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        return objectListing;
    }

    private ObjectListing mockObjectListingWithOnlyDirectories() {
        ObjectListing objectListing = mock(ObjectListing.class);
        List<S3ObjectSummary> summaries = List.of(
            createS3ObjectSummary("directory1/"),
            createS3ObjectSummary("directory2/"),
            createS3ObjectSummary("nested/directory/")
        );
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        return objectListing;
    }

    private ObjectListing mockEmptyObjectListing() {
        ObjectListing objectListing = mock(ObjectListing.class);
        when(objectListing.getObjectSummaries()).thenReturn(List.of());
        return objectListing;
    }

    private S3ObjectSummary createS3ObjectSummary(String key) {
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(key);
        summary.setBucketName(TEST_BUCKET);
        summary.setSize(1024L);
        return summary;
    }
} 