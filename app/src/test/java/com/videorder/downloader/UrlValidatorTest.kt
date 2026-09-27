package com.videorder.downloader

import com.videorder.downloader.utils.UrlValidator
import org.junit.Assert.*
import org.junit.Test

class UrlValidatorTest {

    @Test
    fun testValidHttpAndHttpsUrls() {
        assertTrue(UrlValidator.isValidUrl("https://example.com/video.mp4"))
        assertTrue(UrlValidator.isValidUrl("http://sample.org/stream.webm"))
        assertTrue(UrlValidator.isValidUrl("https://commondatastorage.googleapis.com/test.mov?param=1"))
        assertTrue(UrlValidator.isValidUrl("tg://resolve?domain=test_channel"))

        assertFalse(UrlValidator.isValidUrl(""))
        assertFalse(UrlValidator.isValidUrl("ftp://invalid.com"))
        assertFalse(UrlValidator.isValidUrl("not a url at all"))
    }

    @Test
    fun testExtractUrlFromSharedText() {
        val sharedText = "Hey! Check out this tutorial: https://site.com/tutorial.mp4 hope you enjoy!"
        val extracted = UrlValidator.extractUrlFromText(sharedText)
        assertEquals("https://site.com/tutorial.mp4", extracted)

        val directUrl = "https://media.io/audio.mp3"
        assertEquals(directUrl, UrlValidator.extractUrlFromText(directUrl))

        assertNull(UrlValidator.extractUrlFromText("No url in this message"))
    }

    @Test
    fun testDirectMediaDetection() {
        assertTrue(UrlValidator.isDirectMediaUrl("https://site.com/sample.mp4"))
        assertTrue(UrlValidator.isDirectMediaUrl("https://site.com/sample.webm?token=abc"))
        assertTrue(UrlValidator.isDirectMediaUrl("https://site.com/sample.mkv#section"))
        assertTrue(UrlValidator.isDirectMediaUrl("https://site.com/sample.mp3"))
        assertTrue(UrlValidator.isDirectMediaUrl("https://site.com/document.pdf"))

        assertFalse(UrlValidator.isDirectMediaUrl("https://youtube.com/watch?v=12345"))
        assertFalse(UrlValidator.isDirectMediaUrl("https://news.ycombinator.com"))
    }

    @Test
    fun testExtractFilenameFromUrl() {
        assertEquals("video.mp4", UrlValidator.extractFilenameFromUrl("https://example.com/files/video.mp4"))
        assertEquals("tutorial.mkv", UrlValidator.extractFilenameFromUrl("https://example.com/files/tutorial.mkv?param=true"))
    }
}
