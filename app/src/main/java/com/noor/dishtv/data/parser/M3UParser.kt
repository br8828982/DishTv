package com.noor.dishtv.data.parser

import com.noor.dishtv.data.model.*
import java.io.BufferedReader
import java.io.StringReader
import java.net.URL
import java.util.regex.Pattern

class M3UParser {
    
    companion object {
        private const val M3U_HEADER = "#EXTM3U"
        private const val EXTINF_TAG = "#EXTINF:"
        private const val EXT_X_VERSION = "#EXT-X-VERSION:"
        private const val EXT_X_TARGETDURATION = "#EXT-X-TARGETDURATION:"
        
        // Custom extended attributes for IPTV
        private const val TVG_ID = "tvg-id"
        private const val TVG_NAME = "tvg-name"
        private const val TVG_LOGO = "tvg-logo"
        private const val TVG_COUNTRY = "tvg-country"
        private const val TVG_LANGUAGE = "tvg-language"
        private const val GROUP_TITLE = "group-title"
        private const val TVG_URL = "tvg-url"
        private const val RADIO = "radio"
        private const val USER_AGENT = "user-agent"
        private const val REFERRER = "referrer"
        private const val CODEC = "codec"
        private const val RESOLUTION = "resolution"
        
        // DRM attributes
        private const val DRM_SCHEME = "drm-scheme"
        private const val DRM_LICENSE_URL = "drm-license-url"
        private const val DRM_KEY_ID = "drm-key-id"
        private const val DRM_KEY = "drm-key"
        private const val CLEAR_KEY_ID = "clear-key-id"
        private const val CLEAR_KEY = "clear-key"
        
        // HTTP headers
        private const val HTTP_HEADERS = "http-headers"
        private const val HTTP_COOKIES = "http-cookies"
    }
    
    fun parsePlaylist(content: String, baseUrl: String? = null): List<M3UEntry> {
        val entries = mutableListOf<M3UEntry>()
        val reader = BufferedReader(StringReader(content))
        
        var line: String?
        var currentEntry: M3UEntry? = null
        var isM3UFile = false
        
        while (reader.readLine().also { line = it } != null) {
            line = line?.trim() ?: continue
            
            when {
                line!!.startsWith(M3U_HEADER) -> {
                    isM3UFile = true
                }
                line!!.startsWith(EXTINF_TAG) -> {
                    currentEntry = parseExtInf(line!!)
                }
                line!!.startsWith("#") -> {
                    // Skip other comments and metadata for now
                    continue
                }
                line!!.isNotEmpty() && !line!!.startsWith("#") -> {
                    // This is a URL line
                    if (currentEntry != null) {
                        val finalUrl = resolveUrl(line!!, baseUrl)
                        entries.add(currentEntry.copy(url = finalUrl))
                        currentEntry = null
                    } else if (isM3UFile) {
                        // URL without EXTINF (simple M3U format)
                        val finalUrl = resolveUrl(line!!, baseUrl)
                        val title = extractTitleFromUrl(finalUrl)
                        entries.add(M3UEntry(title = title, url = finalUrl))
                    }
                }
            }
        }
        
        reader.close()
        return entries
    }
    
    private fun parseExtInf(line: String): M3UEntry {
        val content = line.substring(EXTINF_TAG.length)
        val attributes = mutableMapOf<String, String>()
        
        // Parse duration and title
        val parts = content.split(",", limit = 2)
        val duration = if (parts.isNotEmpty()) {
            try {
                parts[0].trim().toDouble()
            } catch (e: NumberFormatException) {
                -1.0
            }
        } else {
            -1.0
        }
        
        val titleAndAttributes = if (parts.size > 1) parts[1] else ""
        
        // Extract attributes from the title part
        val attributePattern = Pattern.compile("""(\w+(?:-\w+)*)="([^"]*)")""")
        val matcher = attributePattern.matcher(titleAndAttributes)
        
        while (matcher.find()) {
            val key = matcher.group(1)?.lowercase() ?: ""
            val value = matcher.group(2) ?: ""
            attributes[key] = value
        }
        
        // Extract the actual title (everything after the last comma that's not an attribute)
        val title = extractTitle(titleAndAttributes, attributes)
        
        // Parse specific attributes
        val logoUrl = attributes[TVG_LOGO]
        val groupTitle = attributes[GROUP_TITLE]
        val language = attributes[TVG_LANGUAGE]
        val country = attributes[TVG_COUNTRY]
        val id = attributes[TVG_ID]
        val epgId = attributes[TVG_ID] // TVG-ID is used for EPG mapping
        val resolution = attributes[RESOLUTION]
        val codec = attributes[CODEC]
        val userAgent = attributes[USER_AGENT]
        val referrer = attributes[REFERRER]
        
        // Parse HTTP headers and cookies
        val headers = parseHeaders(attributes[HTTP_HEADERS])
        val cookies = parseCookies(attributes[HTTP_COOKIES])
        
        // Add user agent and referrer to headers if present
        val finalHeaders = headers.toMutableMap()
        userAgent?.let { finalHeaders["User-Agent"] = it }
        referrer?.let { finalHeaders["Referer"] = it }
        
        // Parse DRM configuration
        val drmConfig = parseDrmConfig(attributes)
        
        return M3UEntry(
            duration = duration,
            title = title,
            url = "", // Will be set when URL line is parsed
            attributes = attributes,
            logoUrl = logoUrl,
            groupTitle = groupTitle,
            language = language,
            country = country,
            id = id,
            epgId = epgId,
            resolution = resolution,
            codec = codec,
            headers = finalHeaders,
            drmConfig = drmConfig
        )
    }
    
    private fun extractTitle(titleAndAttributes: String, attributes: Map<String, String>): String {
        var title = titleAndAttributes
        
        // Remove all parsed attributes from the title
        attributes.forEach { (key, value) ->
            val attributeString = """$key="$value""""
            title = title.replace(attributeString, "")
        }
        
        // Clean up the title
        title = title.replace(Regex("""\s+"""), " ").trim()
        if (title.startsWith(",")) {
            title = title.substring(1).trim()
        }
        
        return if (title.isNotEmpty()) title else "Unknown Channel"
    }
    
    private fun parseHeaders(headersString: String?): Map<String, String> {
        if (headersString.isNullOrEmpty()) return emptyMap()
        
        val headers = mutableMapOf<String, String>()
        headersString.split("|").forEach { header ->
            val parts = header.split(":", limit = 2)
            if (parts.size == 2) {
                headers[parts[0].trim()] = parts[1].trim()
            }
        }
        return headers
    }
    
    private fun parseCookies(cookiesString: String?): Map<String, String> {
        if (cookiesString.isNullOrEmpty()) return emptyMap()
        
        val cookies = mutableMapOf<String, String>()
        cookiesString.split(";").forEach { cookie ->
            val parts = cookie.split("=", limit = 2)
            if (parts.size == 2) {
                cookies[parts[0].trim()] = parts[1].trim()
            }
        }
        return cookies
    }
    
    private fun parseDrmConfig(attributes: Map<String, String>): DrmConfig? {
        val scheme = attributes[DRM_SCHEME]?.let { 
            try {
                DrmScheme.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        } ?: return null
        
        return DrmConfig(
            scheme = scheme,
            licenseUrl = attributes[DRM_LICENSE_URL],
            keyId = attributes[DRM_KEY_ID],
            key = attributes[DRM_KEY],
            clearKeyId = attributes[CLEAR_KEY_ID],
            clearKey = attributes[CLEAR_KEY]
        )
    }
    
    private fun resolveUrl(url: String, baseUrl: String?): String {
        return if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("rtmp://") || url.startsWith("rtsp://")) {
            url
        } else if (baseUrl != null) {
            try {
                URL(URL(baseUrl), url).toString()
            } catch (e: Exception) {
                url
            }
        } else {
            url
        }
    }
    
    private fun extractTitleFromUrl(url: String): String {
        return try {
            val urlObj = URL(url)
            val path = urlObj.path
            val filename = path.substringAfterLast("/")
            if (filename.isNotEmpty()) {
                filename.substringBeforeLast(".")
            } else {
                urlObj.host ?: "Unknown Channel"
            }
        } catch (e: Exception) {
            "Unknown Channel"
        }
    }
    
    fun convertToChannels(entries: List<M3UEntry>, playlistId: Long = 0): List<Channel> {
        return entries.mapIndexed { index, entry ->
            Channel(
                name = entry.title,
                url = entry.url,
                logoUrl = entry.logoUrl,
                group = entry.groupTitle,
                epgId = entry.epgId,
                streamType = detectStreamType(entry.url),
                drmConfig = entry.drmConfig,
                headers = entry.headers,
                language = entry.language,
                country = entry.country,
                resolution = entry.resolution,
                codec = entry.codec,
                sortOrder = index
            )
        }
    }
    
    private fun detectStreamType(url: String): StreamType {
        return when {
            url.contains(".m3u8") || url.contains("m3u8") -> StreamType.HLS
            url.contains(".mpd") || url.contains("dash") -> StreamType.DASH
            url.contains(".ism") || url.contains("smoothstreaming") -> StreamType.SMOOTH_STREAMING
            url.startsWith("rtmp://") -> StreamType.RTMP
            url.startsWith("rtsp://") -> StreamType.RTSP
            else -> StreamType.AUTO
        }
    }
}