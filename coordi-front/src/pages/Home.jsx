import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import WeatherWidget from "../components/WeatherWidget.jsx";
import '../css/Home.css';

const Home = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [weatherComment, setWeatherComment] = useState(null);
    const [outfitRecommendation, setOutfitRecommendation] = useState(null);
    const [weatherData, setWeatherData] = useState(null);

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => setUserInfo(res.data))
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
            case "1": return "맑음";
            case "3": return "구름많음";
            case "4": return "흐림";
            default: return "알 수 없음";
        }
    };

    const fetchOutfitRecommendation = async (age, gender, temperature, weatherCondition, maxTemp, minTemp, rain) => {
        try {
            const response = await axiosInstance.post('/ai-fashion', {
                age, gender, temperature, weatherCondition, maxTemp, minTemp, rain
            });

            console.log("AI API Response:", response.data);
            setWeatherComment(response.data.weatherComment);

            const transformedRecommendation = {};
            Object.keys(response.data.outfitRecommendation).forEach((category) => {
                transformedRecommendation[category] = {
                    item: response.data.outfitRecommendation[category],
                    imageUrls: []
                };
            });

            setOutfitRecommendation(transformedRecommendation);

            await Promise.all(Object.keys(transformedRecommendation).map(category =>
                fetchImagesForCategory(category, transformedRecommendation[category].item)
            ));
        } catch (error) {
            console.error("Outfit recommendation error:", error);
        }
    };

    const fetchImagesForCategory = async (category, item) => {
        if (!item) {
            console.warn(`No item found for category: ${category}`);
            return;
        }

        try {
            const response = await axiosInstance.post(`/crawling`, {
                category,
                item,
                gender: userInfo.gender,
            });
            console.log("Crawling API Response:", response.data);

            setOutfitRecommendation(prevRecommendation => ({
                ...prevRecommendation,
                [category]: {
                    ...prevRecommendation[category],
                    imageUrls: response.data.imageUrls || []
                }
            }));
        } catch (error) {
            console.error(`Error fetching images for ${category}:`, error);
        }
    };


    const handleLogout = () => {
        document.cookie = 'token=; Path=/; Max-Age=0;';
        navigate('/');
    };

    const handlesnap = () =>{
        navigate('/snap')
    }

    return (
        <div className="home-container">
            <h1>홈 화면</h1>
            {userInfo ? (
                <>
                    <WeatherWidget latitude={userInfo.latitude} longitude={userInfo.longitude} setWeatherData={setWeatherData} />
                    <div>
                        <p>안녕하세요, {userInfo.nickname}님!</p>
                        <button onClick={handleLogout}>로그아웃</button>
                        <button onClick={handlesnap}>스냅으로</button>
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
                            {['상의', '하의', '아우터', '신발'].map((category) => {
                                const details = outfitRecommendation[category];
                                if (!details) return null;
                                return (
                                    <div key={category} className="outfit-item">
                                        <h3>{category}</h3>
                                        <p><strong>추천 아이템:</strong> {details.item}</p>
                                        <div className="image-gallery">
                                            {details.imageUrls && details.imageUrls.map((url, index) => (
                                                <img key={index} src={url} alt={`${category} 이미지 ${index + 1}`}
                                                     className="outfit-image"/>
                                            ))}
                                        </div>
                                    </div>
                                );
                            })}
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
