import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import WeatherWidget from "../components/WeatherWidget.jsx";

const Home = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [weatherComment, setWeatherComment] = useState(null);
    const [outfitRecommendation, setOutfitRecommendation] = useState(null);
    const [weatherData, setWeatherData] = useState(null);

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => {
                setUserInfo(res.data);
            })
            .catch(error => {
                console.error("Error:", error);
                alert('유저 정보를 불러오지 못했습니다.');
                navigate('/');
            });
    }, [navigate]);

    useEffect(() => {
        if (userInfo && weatherData) {
            const weatherCondition = getSkyCondition(weatherData.SKY);
            fetchOutfitRecommendation(
                userInfo.age,
                userInfo.gender,
                weatherData.TMP,
                weatherCondition,
                weatherData.TMX,
                weatherData.TMN,
                weatherData.POP
            );
        }
    }, [userInfo, weatherData]);

    const getSkyCondition = (skyValue) => {
        switch (skyValue) {
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

    const fetchOutfitRecommendation = async (age, gender, temperature, weatherCondition, maxTemp, minTemp, rain) => {
        try {
            const response = await axiosInstance.post('/ai-fashion2', {
                age,
                gender,
                temperature,
                weatherCondition,
                maxTemp,
                minTemp,
                rain
            });
            console.log("AI API Response:", response.data);

            setWeatherComment(response.data.weatherComment);
            setOutfitRecommendation(response.data.outfitRecommendation);
        } catch (error) {
            console.error("Outfit recommendation error:", error);
        }
    };

    const handleLogout = () => {
        document.cookie = 'token=; Path=/; Max-Age=0;';
        navigate('/');
    };

    return (
        <div className="home-container">
            <h1>홈 화면</h1>
            {userInfo ? (
                <>
                    <WeatherWidget latitude={userInfo.latitude} longitude={userInfo.longitude} setWeatherData={setWeatherData} />
                    <div>
                        <p>안녕하세요, {userInfo.nickname}님!</p>
                        <button onClick={handleLogout}>로그아웃</button>
                    </div>
                    {weatherComment && (
                        <div className="weather-comment">
                            <h2>오늘의 날씨 코멘트</h2>
                            <p>{weatherComment}</p>
                        </div>
                    )}
                    {outfitRecommendation && (
                        <div className="outfit-recommendation">
                            <h2>오늘의 옷 추천</h2>
                            <ul>
                                <li><strong>상의:</strong> {outfitRecommendation.상의}</li>
                                <li><strong>하의:</strong> {outfitRecommendation.하의}</li>
                                <li><strong>아우터:</strong> {outfitRecommendation.아우터}</li>
                                <li><strong>신발:</strong> {outfitRecommendation.신발}</li>
                            </ul>
                        </div>
                    )}
                </>
            ) : (
                <p>유저 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default Home;
