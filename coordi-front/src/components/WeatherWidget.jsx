import React, { useEffect, useState } from 'react';
import axiosInstance from "../api/axios.jsx";
import "../css/WeatherWidget.css";

const WeatherWidget = ({ latitude, longitude,setWeatherData }) => {

    const [weatherData, setLocalWeatherData] = useState(null);

    useEffect(() => {
        if (latitude && longitude) {
            fetchWeatherData(latitude, longitude);
        }
    }, [latitude, longitude]);

    const fetchWeatherData = async (lat, lon) => {
        try {
            const response = await axiosInstance.post('/weather', {
                latitude: lat,
                longitude: lon
            });
            console.log("Weather API response:", response.data);
            setLocalWeatherData(response.data.values);
            setWeatherData(response.data.values);
        } catch (error) {
            console.error("날씨 데이터를 불러오는 중 오류 발생:", error.response || error.message);
        }
    };


    const getSkyCondition = (value) => {
        switch (value) {
            case "1":
                return "맑음";
            case "3":
                return "구름많음";
            case "4":
                return "흐림";
            default:
                return "알 수 없음";
        }
    };

    return (
        <div className="weather-widget">
            {weatherData ? (
                <>
                    <h2 className="weather-title">현재 날씨</h2>
                    <div className="weather-details">
                        <div className="weather-item">
                            <span className="weather-label">하늘 상태</span>
                            <span className="weather-value">{getSkyCondition(weatherData.SKY)}</span>
                        </div>
                        <div className="weather-item">
                            <span className="weather-label">강수 확률</span>
                            <span className="weather-value">{weatherData.POP}%</span>
                        </div>
                        <div className="weather-item">
                            <span className="weather-label">현재 기온</span>
                            <span className="weather-value">{weatherData.TMP}°C</span>
                        </div>
                        <div className="weather-item">
                            <span className="weather-label">최저 기온</span>
                            <span className="weather-value">{weatherData.TMN}°C</span>
                        </div>
                        <div className="weather-item">
                            <span className="weather-label">최고 기온</span>
                            <span className="weather-value">{weatherData.TMX}°C</span>
                        </div>
                        <div className="weather-item">
                            <span className="weather-label">풍속</span>
                            <span className="weather-value">{weatherData.WSD} m/s</span>
                        </div>
                    </div>
                </>
            ) : (
                <p>날씨 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default WeatherWidget;
