import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axios.jsx';
import '../css/SignUp.css';

const SignUpEx = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { socialId, provider, email } = location.state;
    const [nickname, setNickname] = useState('');
    const [gender, setGender] = useState('');
    const [age, setAge] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');
    const [locationConsent, setLocationConsent] = useState(false);
    const [profileImage, setProfileImage] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);
    const [errors, setErrors] = useState({});

    const validateForm = () => {
        const newErrors = {};
        if (!nickname) newErrors.nickname = '닉네임을 입력해 주세요.';
        if (!gender) newErrors.gender = '성별을 선택해 주세요.';
        if (!age) newErrors.age = '나이를 입력해 주세요.';
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
                () => alert("위치 정보를 가져오지 못했습니다.")
            );
        } else {
            setLatitude('');
            setLongitude('');
        }
    };

    const handleSignUp = async () => {
        if (!validateForm()) return;

        const profileImageBase64 = profileImage
            ? await convertImageToBase64(profileImage)
            : null;

        const memberData = {
            email,
            nickname,
            provider,
            socialId,
            gender,
            age,
            latitude: locationConsent ? latitude : null,
            longitude: locationConsent ? longitude : null,
            profileImageUrl: profileImageBase64,
        };

        axiosInstance.post(`/member/signup/ex`, memberData)
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
            <h1>외부 회원가입</h1>
            <input
                type="text"
                placeholder="닉네임"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="SignUp-InputField"
            />
            {errors.nickname && <span className="error-message">{errors.nickname}</span>}
            <div className="profile-image-container">
                {previewImage ? (
                    <img src={previewImage} alt="Profile Preview" className="profile-image-preview"/>
                ) : (
                    <div className="profile-image-placeholder">프로필 이미지</div>
                )}
                <input type="file" accept="image/*" onChange={handleProfileImageChange}/>
            </div>

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

export default SignUpEx;
