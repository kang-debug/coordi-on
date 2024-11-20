import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import '../css/MySnaps.css';

const MySnaps = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [mySnaps, setMySnaps] = useState([]);
    const [likedSnaps, setLikedSnaps] = useState([]);
    const [activeTab, setActiveTab] = useState('mySnaps');

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => setUserInfo(res.data))
            .catch(error => {
                console.error("Error:", error);
                alert('유저 정보를 불러오지 못했습니다.');
                navigate('/');
            });

        fetchMySnaps();
        fetchLikedSnaps();
    }, [navigate]);

    const fetchMySnaps = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/my-snaps`, { withCredentials: true });
            setMySnaps(res.data);
            console.log(res.data);
        } catch (error) {
            console.error("Error fetching my snaps:", error);
        }
    };

    const fetchLikedSnaps = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/liked-snaps`, { withCredentials: true });
            setLikedSnaps(res.data);
            console.log(res.data);
        } catch (error) {
            console.error("Error fetching liked snaps:", error);
        }
    };

    const handleSnapClick = (snapId) => {
        navigate(`/detail_snap/${snapId}`);
    };

    const handleSetting = () => {
        navigate('/my-setting');
    };


    return (
        <div className="my-snaps-container">
            <h1>나의 스냅 관리</h1>
            {userInfo && (
                <>
                    <button onClick={handleSetting}>설정</button>
                    <div className="profile-section">
                        <img
                            src={userInfo.profileImageUrl}
                            alt="프로필 이미지"
                            className="profile-image"
                        />
                        <p>{userInfo.nickname}님, 환영합니다!</p>
                    </div>
                    <div className="tabs">
                        <button
                            className={activeTab === 'mySnaps' ? 'active' : ''}
                            onClick={() => setActiveTab('mySnaps')}
                        >
                            내가 올린 스냅
                        </button>
                        <button
                            className={activeTab === 'likedSnaps' ? 'active' : ''}
                            onClick={() => setActiveTab('likedSnaps')}
                        >
                            내가 좋아요한 스냅
                        </button>
                    </div>
                    <div className="snaps-list">
                        {activeTab === 'mySnaps' && mySnaps.length > 0 && mySnaps.map(snap => (
                            <div key={snap.snapId} className="snap-item" onClick={() => handleSnapClick(snap.snapId)}>
                                <img src={snap.snapImageUrls[0]} alt="대표 이미지" className="snap-image"/>
                                <p>{snap.snapDescription || "설명이 없습니다."}</p>
                            </div>
                        ))}
                        {activeTab === 'likedSnaps' && likedSnaps.length > 0 && likedSnaps.map(snap => (
                            <div key={snap.snapId} className="snap-item" onClick={() => handleSnapClick(snap.snapId)}>
                                <img src={snap.snapImageUrls[0]} alt="대표 이미지" className="snap-image"/>
                                <p>{snap.snapDescription || "설명이 없습니다."}</p>
                            </div>
                        ))}
                        {(activeTab === 'mySnaps' && mySnaps.length === 0) && <p>아직 업로드한 스냅이 없습니다.</p>}
                        {(activeTab === 'likedSnaps' && likedSnaps.length === 0) && <p>좋아요한 스냅이 없습니다.</p>}
                    </div>
                </>
            )}
        </div>
    );
};

export default MySnaps;
