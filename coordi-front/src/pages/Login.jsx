import React, { useState } from 'react';
import '../css/Login.css';
import KaKao from '../assets/kakao_login_large_wide.png';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";

const Login = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const CLIENT_ID = import.meta.env.VITE_REST_API_KEY;
    const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URL;

    const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;

    const handleLogin = () => {
        if (!email || !password) {
            alert("이메일과 비밀번호를 입력하세요.");
            return;
        }

        axiosInstance.post(`/member/login`, { email, password })
            .then(res => {
                if (res.data.success) {
                    navigate('/home');
                } else {
                    alert('로그인에 실패했습니다.');
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert('로그인 중 오류가 발생했습니다.');
            });
    };
    const findPass = () => {
        navigate('/findpass')
    }
    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
                handleLogin();
        }
    };

    return (
        <div className="Login-container">
            <div className="Login-Logo">
                <h1 className="coordi-on-title">coordi-on</h1>
            </div>
            <div className="Login-Input">
                <input
                    type="email"
                    placeholder="Email"
                    className="Login-InputField"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    className="Login-InputField"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
                <button className="Login-Button" onClick={handleLogin}>로그인</button>
                <button className="Login-Button" onClick={findPass}>비밀번호 찾기</button>
                <button className="Login-Button" onClick={() => navigate('/signup')}>회원가입</button>
                <a href={KAKAO_AUTH_URL} className="kakaobtn">
                    <img src={KaKao} alt="Kakao login button"/>
                </a>
            </div>
        </div>
    );
};

export default Login;
