import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";

const Snap = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => setUserInfo(res.data))
            .catch(error => {
                console.error("Error:", error);
                alert('유저 정보를 불러오지 못했습니다.');
                navigate('/');
            });
    }, [navigate]);
    
    
    const handleLogout = () => {
        document.cookie = 'token=; Path=/; Max-Age=0;';
        navigate('/');
    };

    const goupload = () => {
        navigate('/upload_snap')
    }

    const godetail = () => {
        navigate('/detail_snap')
    }

    return (
        <div className="home-container">
            <h1>스냅 화면</h1>
            {userInfo ? (
                <>
                    <p>안녕하세요, {userInfo.nickname}님!</p>
                    <button onClick={handleLogout}>로그아웃</button>
                    <button onClick={goupload}>스냅 등록하기</button>
                    <button onClick={godetail}>스냅 디테일</button>

                </>
            ) : (
                <p>유저 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default Snap;
