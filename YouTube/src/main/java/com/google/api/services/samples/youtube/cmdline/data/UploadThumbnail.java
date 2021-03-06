package com.google.api.services.samples.youtube.cmdline.data;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.samples.youtube.cmdline.Auth;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Thumbnails.Set;
import com.google.api.services.youtube.model.ThumbnailSetResponse;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.List;

/**
 * set method to set the image as the custom thumbnail for a video.
 */
public class UploadThumbnail {

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Define a global variable that specifies the MIME type of the image
     * being uploaded.
     */
    private static final String IMAGE_FILE_FORMAT = "image/png";

    /**
     * Prompt the user to specify a video ID and the path for a thumbnail
     * image. Then call the API to set the image as the thumbnail for the video.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "uploadthumbnail");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-uploadthumbnail-sample").build();

            // Prompt the user to enter the video ID of the video being updated.
            String videoId = getVideoIdFromUser();
            System.out.println("You chose " + videoId + " to upload a thumbnail.");

            // Prompt the user to specify the location of the thumbnail image.
            File imageFile = getImageFromUser();
            System.out.println("You chose " + imageFile + " to upload.");

            // Create an object that contains the thumbnail image file's
            // contents.
            InputStreamContent mediaContent = new InputStreamContent(
                    IMAGE_FILE_FORMAT, new BufferedInputStream(new FileInputStream(imageFile)));
            mediaContent.setLength(imageFile.length());

            // Create an API request that specifies that the mediaContent
            // object is the thumbnail of the specified video.
            Set thumbnailSet = youtube.thumbnails().set(videoId, mediaContent);

            // Set the upload type and add an event listener.
            MediaHttpUploader uploader = thumbnailSet.getMediaHttpUploader();

            // based on true and false media will upload like if true directly will upload , if it is false we resume and upload because of getting network issue
            uploader.setDirectUploadEnabled(false);

            // Set the upload state for the thumbnail image.
            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        // This value is set before the initiation request is
                        // sent.
                        case INITIATION_STARTED:
                            System.out.println("Initiation Started");
                            break;
                        // This value is set after the initiation request
                        //  completes.
                        case INITIATION_COMPLETE:
                            System.out.println("Initiation Completed");
                            break;
                        // This value is set after a media file chunk is
                        // uploaded.
                        case MEDIA_IN_PROGRESS:
                            System.out.println("Upload in progress");
                            System.out.println("Upload percentage: " + uploader.getProgress());
                            break;
                        // This value is set after the entire media file has
                        //  been successfully uploaded.
                        case MEDIA_COMPLETE:
                            System.out.println("Upload Completed!");
                            break;
                        // This value indicates that the upload process has
                        //  not started yet.
                        case NOT_STARTED:
                            System.out.println("Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Upload the image and set it as the specified video's thumbnail.
            ThumbnailSetResponse setResponse = thumbnailSet.execute();

            // Print the URL for the updated video's thumbnail image.
            System.out.println("\n================== Uploaded Thumbnail ==================\n");
            System.out.println("  - Url: " + setResponse.getItems().get(0).getDefault().getUrl());

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Prompts the user to enter a YouTube video ID and return the user input.
     */
    private static String getVideoIdFromUser() throws IOException {

        String inputVideoId = "";

        System.out.print("Please enter a video Id to update: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputVideoId = bReader.readLine();

        if (inputVideoId.length() < 1) {
            // check the count provide by user is less than one then can't upload those details .
            System.out.print("Video Id can't be empty!");
            System.exit(1);
        }

        return inputVideoId;
    }

    /*
     * Prompt the user to enter the path for the thumbnail image being uploaded.
     */
    private static File getImageFromUser() throws IOException {

        String path = "";

        System.out.print("Please enter the path of the image file to upload: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        path = bReader.readLine();

        if (path.length() < 1) {
        	// check the count provide by user is less than one then can't upload those details .
            System.out.print("Path can not be empty!");
            System.exit(1);
        }

        return new File(path);
    }
}
