import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import '../css/SnapUp.css';

const SnapUp = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [description, setDescription] = useState('');
    const [images, setImages] = useState([]);
    const [categories, setCategories] = useState([]);
    const availableCategories = ["캐주얼", "스트릿", "빈티지", "모던", "댄디", "스포티", "꾸안꾸", "꾸꾸꾸"];

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => {
                console.log("User Data:", res.data);
                setUserInfo(res.data);
            })
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

    const handleImageChange = (event) => {
        const files = Array.from(event.target.files);
        setImages((prevImages) => [...prevImages, ...files]);
    };

    const removeImage = (index) => {
        setImages(images.filter((_, i) => i !== index));
    };

    const handleCategoryChange = (category) => {
        setCategories(prevCategories =>
            prevCategories.includes(category)
                ? prevCategories.filter(c => c !== category)
                : [...prevCategories, category]
        );
    };

    const handleSubmit = async () => {
        if (images.length === 0 || categories.length === 0) {
            alert("이미지와 카테고리를 선택했는지 확인해주세요.");
            return;
        }

        const formData = new FormData();
        images.forEach(image => formData.append('images', image));
        formData.append('description', description || '');
        formData.append('memberId', userInfo.memberId);
        formData.append('categories', JSON.stringify(categories));

        try {
            await axiosInstance.post('/snaps/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            alert('스냅이 성공적으로 업로드되었습니다.');
            navigate('/snap');
        } catch (error) {
            console.error("Snap upload error:", error);
            if (error.response?.data) {
                alert(`서버 오류: ${error.response.data}`);
            } else {
                alert('스냅 업로드 중 알 수 없는 오류가 발생했습니다.');
            }
        }
    };

    return (
        <div className="snap-upload-container">
            <h1>스냅 등록하기</h1>
            {userInfo ? (
                <>
                    <p>안녕하세요, {userInfo.nickname}님!</p>
                    <button onClick={handleLogout}>로그아웃</button>
                    <textarea
                        placeholder="스냅 내용 (선택사항)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                    <input type="file" multiple accept="image/*" onChange={handleImageChange} />
                    <div className="image-preview">
                        {images.map((image, index) => (
                            <div key={index} className="image-container">
                                <img src={URL.createObjectURL(image)} alt={`preview-${index}`} className="preview-img" />
                                <button type="button" onClick={() => removeImage(index)} className="remove-image">X</button>
                            </div>
                        ))}
                    </div>
                    <div className="category-selection">
                        {availableCategories.map((category) => (
                            <label key={category}>
                                <input
                                    type="checkbox"
                                    value={category}
                                    checked={categories.includes(category)}
                                    onChange={() => handleCategoryChange(category)}
                                />
                                {category}
                            </label>
                        ))}
                    </div>
                    <button onClick={handleSubmit}>업로드</button>
                </>
            ) : (
                <p>유저 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default SnapUp;
