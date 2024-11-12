import React, { useState } from 'react';
import '../css/Login.css';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";

const FindPass = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [password, setPassword] = useState('');
    const [step, setStep] = useState(1); // 단계 상태: 1 = 이메일 입력, 2 = 코드 확인, 3 = 비밀번호 재설정

    const sendVerificationCode = async () => {
        if (!email) {
            alert('이메일을 입력하세요.');
            return;
        }
        try {
            await axiosInstance.post('/member/send-code', { email, purpose: "reset-password"  });
            alert("이메일로 인증 코드를 전송했습니다.");
            setStep(2);
        } catch (error) {
            console.error("Error sending verification code:", error);
            alert("코드 전송에 실패했습니다. 이메일을 다시 확인해주세요.");
        }
    };

    const verifyCode = async () => {
        try {
            const response = await axiosInstance.post('/member/verify-code', { email, code: verificationCode });
            if (response.data.success) {
                alert("코드가 확인되었습니다. 비밀번호를 재설정해주세요.");
                setStep(3);
            } else {
                alert("코드가 일치하지 않습니다. 다시 시도해주세요.");
            }
        } catch (error) {
            console.error("Error verifying code:", error);
            alert("코드 확인에 실패했습니다.");
        }
    };

    const resetPassword = async () => {
        try {
            await axiosInstance.post('/member/reset-password', { email, newPassword: password });
            alert("비밀번호가 성공적으로 재설정되었습니다.");
            navigate('/');
        } catch (error) {
            console.error("Error resetting password:", error);
            alert("비밀번호 재설정에 실패했습니다.");
        }
    };

    return (
        <div className="Login-container">
            <div className="Login-Logo">
                <h1 className="coordi-on-title">coordi-on</h1>
            </div>
            {step === 1 && (
                <div className="Login-Input">
                    <input
                        type="email"
                        placeholder="Email"
                        className="Login-InputField"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <button onClick={sendVerificationCode} className="Login-Button">이메일 코드 보내기</button>
                </div>
            )}
            {step === 2 && (
                <div className="Login-Input">
                    <input
                        type="text"
                        placeholder="Verification Code"
                        className="Login-InputField"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                    />
                    <button onClick={verifyCode} className="Login-Button">코드 확인</button>
                </div>
            )}
            {step === 3 && (
                <div className="Login-Input">
                    <input
                        type="password"
                        placeholder="New Password"
                        className="Login-InputField"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <button onClick={resetPassword} className="Login-Button">비밀번호 재설정</button>
                </div>
            )}
        </div>
    );
};

export default FindPass;
