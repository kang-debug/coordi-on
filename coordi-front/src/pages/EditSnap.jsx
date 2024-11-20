import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import '../css/EditSnap.css';

const EditSnap = () => {
    const { snapId } = useParams();
    const navigate = useNavigate();
    const [snapData, setSnapData] = useState(null);
    const [description, setDescription] = useState('');
    const [categories, setCategories] = useState([]);
    const [imageUrls, setImageUrls] = useState([]);
    const [removedImageUrls, setRemovedImageUrls] = useState([]);
    const [removedImageIndexes, setRemovedImageIndexes] = useState([]);
    const [newImages, setNewImages] = useState([]);
    const [allCategories] = useState([
        "캐주얼", "스트릿", "빈티지", "모던", "댄디", "스포티", "꾸안꾸", "꾸꾸꾸"
    ]);

    useEffect(() => {
        fetchSnapDetails();
    }, [snapId]);

    const fetchSnapDetails = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/${snapId}`);
            setSnapData(res.data);
            setDescription(res.data.snapDescription || '');
            setCategories(res.data.categories || []);
            setImageUrls(res.data.snapImageUrls || []);
        } catch (error) {
            console.error("Error fetching snap details:", error);
            alert("스냅 정보를 불러오는 중 오류가 발생했습니다.");
        }
    };

    const handleCategoryToggle = (category) => {
        if (categories.includes(category)) {
            setCategories(categories.filter((c) => c !== category));
        } else {
            setCategories([...categories, category]);
        }
    };

    const handleNewImageChange = (e) => {
        setNewImages([...newImages, ...Array.from(e.target.files)]);
    };

    const handleRemoveExistingImage = (index) => {
        setRemovedImageUrls([...removedImageUrls, imageUrls[index]]);
        setImageUrls(imageUrls.filter((_, i) => i !== index));
    };

    const handleRemoveNewImage = (index) => {
        setNewImages(newImages.filter((_, i) => i !== index));
    };

    const validateForm = () => {
        if (imageUrls.length - removedImageIndexes.length + newImages.length === 0) {
            alert("적어도 하나의 이미지를 업로드해야 합니다.");
            return false;
        }
        if (categories.length === 0) {
            alert("적어도 하나의 카테고리를 선택해야 합니다.");
            return false;
        }
        return true;
    };


    const handleSaveChanges = async () => {
        if (!validateForm()) return;

        const formData = new FormData();
        formData.append('description', description || '');
        formData.append('categories', JSON.stringify(categories));

        removedImageUrls.forEach((url) => formData.append('removedImageUrls', url));

        newImages.forEach((image) => formData.append('newImages', image));

        try {
            await axiosInstance.put(`/snaps/${snapId}`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            alert("스냅이 수정되었습니다.");
            navigate(`/detail_snap/${snapId}`);
        } catch (error) {
            console.error("Error updating snap:", error);
            alert("스냅 수정 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="edit-snap-container">
            <h1>스냅 수정</h1>
            {snapData ? (
                <>
                    <div className="edit-snap-section">
                        <label htmlFor="description">설명</label>
                        <textarea
                            id="description"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="스냅 설명을 입력하세요 (선택 사항)"
                        />
                    </div>
                    <div className="edit-snap-section">
                        <h3>카테고리</h3>
                        <div className="categories">
                            {allCategories.map((category) => (
                                <label key={category} className="category-label">
                                    <input
                                        type="checkbox"
                                        checked={categories.includes(category)}
                                        onChange={() => handleCategoryToggle(category)}
                                    />
                                    {category}
                                </label>
                            ))}
                        </div>
                    </div>
                    <div className="edit-snap-section">
                        <h3>기존 이미지</h3>
                        <div className="existing-images">
                            {imageUrls.map((url, index) => (
                                !removedImageIndexes.includes(index) && (
                                    <div key={index} className="image-container">
                                        <img src={url} alt={`Snap ${index + 1}`} className="preview-img" />
                                        <button
                                            type="button"
                                            className="remove-image"
                                            onClick={() => handleRemoveExistingImage(index)}
                                        >
                                            X
                                        </button>
                                    </div>
                                )
                            ))}
                        </div>
                        <h3>새로운 이미지 추가</h3>
                        <input
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handleNewImageChange}
                        />
                        <div className="image-preview">
                            {newImages.map((image, index) => (
                                <div key={index} className="image-container">
                                    <img src={URL.createObjectURL(image)} alt={`New Snap ${index + 1}`} className="preview-img" />
                                    <button
                                        type="button"
                                        className="remove-image"
                                        onClick={() => handleRemoveNewImage(index)}
                                    >
                                        X
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>
                    <button className="save-button" onClick={handleSaveChanges}>
                        저장
                    </button>
                    <button className="cancel-button" onClick={() => navigate(-1)}>
                        취소
                    </button>
                </>
            ) : (
                <p>스냅 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default EditSnap;
