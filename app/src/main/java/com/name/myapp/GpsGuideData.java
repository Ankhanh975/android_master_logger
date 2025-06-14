package com.name.myapp;

/**
 * GPS Guide Data - Contains all documentation and guide text for GPS accuracy information
 */
public class GpsGuideData {
    
    /**
     * Returns the complete GPS accuracy guide text
     * @return Complete GPS guide documentation
     */
    public static String getGpsGuideText() {
        return 
            "📍 GPS ACCURACY GUIDE\n\n" +
            "🎯 LOCATION ACCURACY\n" +
            "• Excellent: ±1-3 meters (open sky, clear view)\n" +
            "• Good: ±3-5 meters (urban areas, some obstructions)\n" +
            "• Fair: ±5-10 meters (dense urban, heavy tree cover)\n" +
            "• Poor: ±10+ meters (indoor, underground, tunnels)\n\n" +
            "📊 ALTITUDE ACCURACY\n" +
            "• GPS altitude is less accurate than horizontal position\n" +
            "• Typical accuracy: ±10-20 meters (vs ±3-5m horizontal)\n" +
            "• Barometric sensors improve altitude accuracy\n" +
            "• Elevation data may vary by ±15-30 meters\n\n" +
            "🚗 SPEED ACCURACY\n" +
            "• Excellent: ±0.5-1 km/h (constant movement)\n" +
            "• Good: ±1-2 km/h (variable speed)\n" +
            "• Poor: ±2-5 km/h (slow movement, stops/starts)\n" +
            "• Speed below 2 km/h may show as 0\n\n" +
            "📡 SATELLITE SIGNAL STRENGTH (dB-Hz)\n" +
            "• Excellent: 40+ dB-Hz (strong, clear signal)\n" +
            "• Good: 30-40 dB-Hz (reliable positioning)\n" +
            "• Fair: 20-30 dB-Hz (usable, may be less accurate)\n" +
            "• Poor: <20 dB-Hz (weak, unreliable)\n\n" +
            "🛰️ SATELLITE CONSTELLATIONS\n" +
            "• GPS (USA): 24+ satellites, global coverage\n" +
            "• GLONASS (Russia): 24 satellites, global coverage\n" +
            "• BeiDou (China): 35+ satellites, global coverage\n" +
            "• Galileo (Europe): 30 satellites, global coverage\n" +
            "• QZSS (Japan): 4 satellites, Asia-Pacific focus\n" +
            "• SBAS: Augmentation systems for improved accuracy\n\n" +
            "📈 OPTIMAL SATELLITE CONDITIONS\n" +
            "• Minimum satellites: 4 for basic positioning\n" +
            "• Good positioning: 6-8 satellites\n" +
            "• Excellent positioning: 8+ satellites\n" +
            "• Multi-constellation: Better accuracy and reliability\n\n" +
            "🌍 FACTORS AFFECTING ACCURACY\n" +
            "• Atmospheric conditions (ionosphere, troposphere)\n" +
            "• Satellite geometry (HDOP, VDOP, PDOP)\n" +
            "• Multipath interference (buildings, trees)\n" +
            "• Device hardware quality\n" +
            "• Environmental obstructions\n\n" +
            "⚡ REAL-TIME ACCURACY INDICATORS\n" +
            "• \"Used\" satellites: Actually contributing to position fix\n" +
            "• \"Strong\" signals: >30 dB-Hz, high-quality data\n" +
            "• First Fix Time: Time to acquire initial position\n" +
            "• GNSS Status: System operational state\n\n" +
            "🔧 IMPROVING ACCURACY\n" +
            "• Clear view of sky (minimize obstructions)\n" +
            "• Wait for more satellites to acquire\n" +
            "• Stay stationary for initial fix\n" +
            "• Use high-accuracy mode\n" +
            "• Enable all available constellations\n\n" +
            "📱 DEVICE-SPECIFIC CONSIDERATIONS\n" +
            "• Modern phones support multiple constellations\n" +
            "• Hardware quality varies between devices\n" +
            "• Some devices have barometric sensors\n" +
            "• Antenna quality affects signal reception\n" +
            "• Software algorithms improve accuracy\n\n" +
            "⚠️ LIMITATIONS\n" +
            "• Indoor positioning is unreliable\n" +
            "• Urban canyons reduce accuracy\n" +
            "• Weather can affect signal quality\n" +
            "• Battery optimization may reduce update frequency\n" +
            "• Some features require clear sky view";
    }
} 