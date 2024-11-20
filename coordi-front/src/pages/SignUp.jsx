import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axios.jsx';
import '../css/SignUp.css';

const SignUp = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [nickname, setNickname] = useState('');
    const [gender, setGender] = useState('');
    const [age, setAge] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');
    const [locationConsent, setLocationConsent] = useState(false);
    const [inputVerificationCode, setInputVerificationCode] = useState('');
    const [isVerified, setIsVerified] = useState(false);
    const [profileImage, setProfileImage] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);
    const [errors, setErrors] = useState({});
    const navigate = useNavigate();

    const isEmailValid = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    const validateForm = () => {
        const newErrors = {};
        if (!email) newErrors.email = '이메일을 입력해 주세요.';
        else if (!isEmailValid(email)) newErrors.email = '유효한 이메일 형식을 입력해 주세요.';
        if (!password) newErrors.password = '비밀번호를 입력해 주세요.';
        if (!confirmPassword) newErrors.confirmPassword = '비밀번호 확인을 입력해 주세요.';
        else if (password !== confirmPassword) newErrors.confirmPassword = '비밀번호가 일치하지 않습니다.';
        if (!nickname) newErrors.nickname = '닉네임을 입력해 주세요.';
        if (!gender) newErrors.gender = '성별을 선택해 주세요.';
        if (!age) newErrors.age = '나이를 입력해 주세요.';
        if (locationConsent && (!latitude || !longitude)) newErrors.location = '위치 정보를 입력해 주세요.';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleProfileImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setProfileImage(file);
            setPreviewImage(URL.createObjectURL(file));
        }
    };

    const convertImageToBase64 = (file) => {
        return new Promise((resolve, reject) => {
            if (!file) {
                resolve(null);
                return;
            }
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = (error) => reject(error);
            reader.readAsDataURL(file);
        });
    };

    const handleLocationConsent = () => {
        setLocationConsent(!locationConsent);
        if (!locationConsent) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    setLatitude(latitude);
                    setLongitude(longitude);
                },
                () => alert('위치 정보를 가져오지 못했습니다.'),
                { enableHighAccuracy: true }
            );
        } else {
            setLatitude('');
            setLongitude('');
        }
    };

    const handleSendCode = () => {
        if (!email) {
            setErrors((prevErrors) => ({ ...prevErrors, email: '이메일을 입력하세요.' }));
            return;
        }
        axiosInstance.post(`/member/send-code`, { email, purpose: 'signup' })
            .then(() => {
                alert('인증 코드가 이메일로 전송되었습니다.');
                setErrors((prevErrors) => ({ ...prevErrors, email: null }));
            })
            .catch((error) => {
                setErrors((prevErrors) => ({ ...prevErrors, email: error.response?.data?.error || '에러가 발생했습니다.' }));
            });
    };

    const handleVerifyCode = () => {
        if (!inputVerificationCode) {
            alert('인증 코드를 입력하세요.');
            return;
        }
        axiosInstance.post(`/member/verify-code`, { email, code: inputVerificationCode })
            .then(() => {
                setIsVerified(true);
                alert('이메일 인증이 완료되었습니다.');
            })
            .catch((error) => {
                console.error('Error:', error);
                alert('인증 코드가 올바르지 않습니다.');
            });
    };

    const handleSignUp = async () => {
        if (!validateForm()) return;

        if (!isVerified) {
            alert('이메일 인증이 필요합니다.');
            return;
        }

        const profileImageBase64 = profileImage
            ? await convertImageToBase64(profileImage)
            : null;

        const memberData = {
            email,
            password,
            nickname,
            gender,
            age,
            latitude: locationConsent ? latitude : null,
            longitude: locationConsent ? longitude : null,
            profileImageUrl: profileImageBase64,
        };

        axiosInstance.post(`/member/signup`, memberData)
            .then((res) => {
                if (res.data.success) {
                    alert('회원가입에 성공했습니다.');
                    navigate('/');
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                alert('회원가입 중 오류가 발생했습니다.');
            });
    };

    return (
        <div className="SignUp-container">
            <h1>일반 회원가입</h1>
            {errors.general && <span className="error-message general-error">{errors.general}</span>}
            <input
                type="text"
                placeholder="이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.email && <span className="error-message">{errors.email}</span>}

            <button onClick={handleSendCode} className="SendCode-Button">인증 코드 전송</button>

            <input
                type="text"
                placeholder="인증 코드 입력"
                value={inputVerificationCode}
                onChange={(e) => setInputVerificationCode(e.target.value)}
                className="SignUp-InputField"
            />
            <button onClick={handleVerifyCode} className="VerifyCode-Button">인증 코드 확인</button>

            <input
                type="password"
                placeholder="비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.password && <span className="error-message">{errors.password}</span>}

            <input
                type="password"
                placeholder="비밀번호 확인"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.confirmPassword && <span className="error-message">{errors.confirmPassword}</span>}

            <div className="profile-image-container">
                {previewImage ? (
                    <img src={previewImage} alt="Profile Preview" className="profile-image-preview" />
                ) : (
                    <div className="profile-image-placeholder">프로필 이미지</div>
                )}
                <input type="file" accept="image/*" onChange={handleProfileImageChange} />
            </div>

            <input
                type="text"
                placeholder="닉네임"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.nickname && <span className="error-message">{errors.nickname}</span>}

            <select
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="SignUp-InputField"
            >
                <option value="">성별 선택</option>
                <option value="남성">남성</option>
                <option value="여성">여성</option>
            </select>
            {errors.gender && <span className="error-message">{errors.gender}</span>}

            <input
                type="number"
                placeholder="나이"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.age && <span className="error-message">{errors.age}</span>}

            <input
                type="checkbox"
                checked={locationConsent}
                onChange={handleLocationConsent}
            />
            <label>위치 정보 제공에 동의합니다</label>

            {locationConsent && (
                <div className="SignUp-location-fields">
                    <input
                        type="text"
                        placeholder="위도"
                        value={latitude}
                        onChange={(e) => setLatitude(e.target.value)}
                        className="SignUp-InputField"
                    />
                    <input
                        type="text"
                        placeholder="경도"
                        value={longitude}
                        onChange={(e) => setLongitude(e.target.value)}
                        className="SignUp-InputField"
                    />
                </div>
            )}
            {errors.location && <span className="error-message">{errors.location}</span>}

            <button onClick={handleSignUp} className="SignUp-Button">회원가입 완료</button>
        </div>
    );
};

export default SignUp;
