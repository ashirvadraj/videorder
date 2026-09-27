package com.videorder.downloader

import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.Formatters
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class FileUtilsTest {

    @Test
    fun testFilenameSanitization() {
        val dangerous = "video/test:sample*name?with\"illegal<chars>|end.mp4"
        val sanitized = FileUtils.sanitizeFilename(dangerous)
        assertFalse(sanitized.contains("/"))
        assertFalse(sanitized.contains(":"))
        assertFalse(sanitized.contains("*"))
        assertFalse(sanitized.contains("?"))
        assertFalse(sanitized.contains("\""))
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
        assertFalse(sanitized.contains("|"))
    }

    @Test
    fun testMediaTypeDetection() {
        assertEquals(MediaType.VIDEO, FileUtils.getMediaTypeFromMimeOrExt("video/mp4", "video.mp4"))
        assertEquals(MediaType.VIDEO, FileUtils.getMediaTypeFromMimeOrExt("application/octet-stream", "recording.mkv"))
        assertEquals(MediaType.AUDIO, FileUtils.getMediaTypeFromMimeOrExt("audio/mpeg", "song.mp3"))
        assertEquals(MediaType.IMAGE, FileUtils.getMediaTypeFromMimeOrExt("image/jpeg", "photo.jpg"))
        assertEquals(MediaType.DOCUMENT, FileUtils.getMediaTypeFromMimeOrExt("application/pdf", "manual.pdf"))
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", Formatters.formatBytes(0L))
        assertEquals("500 B", Formatters.formatBytes(500L))
        assertTrue(Formatters.formatBytes(1024L).contains("KB"))
        assertTrue(Formatters.formatBytes(1024L * 1024L * 250L).contains("MB"))
        assertTrue(Formatters.formatBytes(1024L * 1024L * 1024L * 2L).contains("GB"))
    }

    @Test
    fun testDateBucketTodayAndYesterday() {
        val now = System.currentTimeMillis()
        assertEquals("Today", Formatters.getDateBucket(now))

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        assertEquals("Yesterday", Formatters.getDateBucket(yesterdayCal.timeInMillis))
    }
}
