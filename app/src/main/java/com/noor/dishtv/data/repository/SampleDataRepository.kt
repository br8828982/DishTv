package com.noor.dishtv.data.repository

import com.noor.dishtv.data.database.ChannelDao
import com.noor.dishtv.data.database.PlaylistDao
import com.noor.dishtv.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataRepository @Inject constructor(
    private val channelDao: ChannelDao,
    private val playlistDao: PlaylistDao
) {
    
    suspend fun insertSampleData() {
        // Check if we already have data
        val existingChannels = channelDao.getChannelCount()
        if (existingChannels > 0) return
        
        // Insert sample channels for demonstration
        val sampleChannels = listOf(
            Channel(
                name = "BBC News HD",
                url = "https://d2vnbkvjbims7j.cloudfront.net/containerA/LTN/playlist.m3u8",
                logoUrl = "https://i.imgur.com/REuN9RR.png",
                group = "News",
                language = "English",
                country = "UK",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 1
            ),
            Channel(
                name = "CNN International",
                url = "https://turnerlive.warnermediacdn.com/hls/live/586495/cnngo/cnn_slate/VIDEO_0_3564000.m3u8",
                logoUrl = "https://i.imgur.com/ilZJT5s.png",
                group = "News",
                language = "English",
                country = "US",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 2
            ),
            Channel(
                name = "Al Jazeera English",
                url = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8",
                logoUrl = "https://i.imgur.com/7bRVpnu.png",
                group = "News",
                language = "English",
                country = "Qatar",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 3
            ),
            Channel(
                name = "France 24 English",
                url = "https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8",
                logoUrl = "https://i.imgur.com/ChzOf59.png",
                group = "News",
                language = "English",
                country = "France",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 4
            ),
            Channel(
                name = "DW English",
                url = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
                logoUrl = "https://i.imgur.com/A1xzjOI.png",
                group = "News",
                language = "English",
                country = "Germany",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 5
            ),
            Channel(
                name = "Red Bull TV",
                url = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_928.m3u8",
                logoUrl = "https://i.imgur.com/TmDcIxC.png",
                group = "Sports",
                language = "English",
                country = "Austria",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 6
            ),
            Channel(
                name = "NASA TV",
                url = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://i.imgur.com/PjSq1yK.png",
                group = "Educational",
                language = "English",
                country = "US",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 7
            ),
            Channel(
                name = "Fashion TV",
                url = "https://fashiontv-fashiontv-5-nl.samsung.wurl.tv/playlist.m3u8",
                logoUrl = "https://i.imgur.com/iIU9r2g.png",
                group = "Lifestyle",
                language = "English",
                country = "France",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 8
            ),
            Channel(
                name = "Bloomberg TV",
                url = "https://bloomberg.com/media-manifest/streams/phoenix-us.m3u8",
                logoUrl = "https://i.imgur.com/OuogLHX.png",
                group = "Business",
                language = "English",
                country = "US",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 9
            ),
            Channel(
                name = "Euronews",
                url = "https://rakuten-euronews-1-gb.samsung.wurl.tv/manifest/playlist.m3u8",
                logoUrl = "https://i.imgur.com/7V012zQ.png",
                group = "News",
                language = "English",
                country = "France",
                resolution = "HD",
                streamType = StreamType.HLS,
                sortOrder = 10
            ),
            
            // Example channels with DRM (for demonstration - these URLs are fictional)
            Channel(
                name = "Premium Sports HD",
                url = "https://example.com/premium-sports/manifest.mpd",
                logoUrl = "https://i.imgur.com/sports_logo.png",
                group = "Premium Sports",
                language = "English",
                country = "US",
                resolution = "4K",
                streamType = StreamType.DASH,
                drmConfig = DrmConfig(
                    scheme = DrmScheme.WIDEVINE,
                    licenseUrl = "https://example.com/widevine-license",
                    headers = mapOf(
                        "Authorization" -> "Bearer sample_token",
                        "X-Custom-Header" -> "premium_user"
                    )
                ),
                headers = mapOf(
                    "User-Agent" -> "DishTV IPTV Player",
                    "Referer" -> "https://dishtv.com"
                ),
                sortOrder = 11
            ),
            Channel(
                name = "Movies 4K ClearKey",
                url = "https://example.com/movies-4k/playlist.m3u8",
                logoUrl = "https://i.imgur.com/movies_logo.png",
                group = "Movies",
                language = "English",
                country = "US",
                resolution = "4K",
                streamType = StreamType.HLS,
                drmConfig = DrmConfig(
                    scheme = DrmScheme.CLEARKEY,
                    clearKeyId = "sample_key_id_base64",
                    clearKey = "sample_key_value_base64"
                ),
                sortOrder = 12
            )
        )
        
        // Insert channels
        channelDao.insertChannels(sampleChannels)
        
        // Create a sample playlist
        val samplePlaylist = Playlist(
            name = "Demo IPTV Playlist",
            url = "https://example.com/demo.m3u8",
            type = PlaylistType.M3U8,
            channelCount = sampleChannels.size,
            lastUpdated = System.currentTimeMillis(),
            isEnabled = true,
            sortOrder = 1,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        
        playlistDao.insertPlaylist(samplePlaylist)
    }
    
    fun getSampleM3UContent(): String {
        return """
            #EXTM3U
            #EXTINF:-1 tvg-id="bbc-news" tvg-name="BBC News HD" tvg-logo="https://i.imgur.com/REuN9RR.png" group-title="News",BBC News HD
            https://d2vnbkvjbims7j.cloudfront.net/containerA/LTN/playlist.m3u8
            
            #EXTINF:-1 tvg-id="cnn-int" tvg-name="CNN International" tvg-logo="https://i.imgur.com/ilZJT5s.png" group-title="News",CNN International
            https://turnerlive.warnermediacdn.com/hls/live/586495/cnngo/cnn_slate/VIDEO_0_3564000.m3u8
            
            #EXTINF:-1 tvg-id="aljazeera" tvg-name="Al Jazeera English" tvg-logo="https://i.imgur.com/7bRVpnu.png" group-title="News",Al Jazeera English
            https://live-hls-web-aje.getaj.net/AJE/01.m3u8
            
            #EXTINF:-1 tvg-id="france24" tvg-name="France 24 English" tvg-logo="https://i.imgur.com/ChzOf59.png" group-title="News",France 24 English
            https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8
            
            #EXTINF:-1 tvg-id="dw" tvg-name="DW English" tvg-logo="https://i.imgur.com/A1xzjOI.png" group-title="News",DW English
            https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8
            
            #EXTINF:-1 tvg-id="redbull" tvg-name="Red Bull TV" tvg-logo="https://i.imgur.com/TmDcIxC.png" group-title="Sports",Red Bull TV
            https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_928.m3u8
            
            #EXTINF:-1 tvg-id="nasa" tvg-name="NASA TV" tvg-logo="https://i.imgur.com/PjSq1yK.png" group-title="Educational",NASA TV
            https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8
            
            #EXTINF:-1 tvg-id="ftv" tvg-name="Fashion TV" tvg-logo="https://i.imgur.com/iIU9r2g.png" group-title="Lifestyle",Fashion TV
            https://fashiontv-fashiontv-5-nl.samsung.wurl.tv/playlist.m3u8
            
            #EXTINF:-1 tvg-id="bloomberg" tvg-name="Bloomberg TV" tvg-logo="https://i.imgur.com/OuogLHX.png" group-title="Business",Bloomberg TV
            https://bloomberg.com/media-manifest/streams/phoenix-us.m3u8
            
            #EXTINF:-1 tvg-id="euronews" tvg-name="Euronews" tvg-logo="https://i.imgur.com/7V012zQ.png" group-title="News",Euronews
            https://rakuten-euronews-1-gb.samsung.wurl.tv/manifest/playlist.m3u8
        """.trimIndent()
    }
}