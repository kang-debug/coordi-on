import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import '../css/Snap.css';

const Snap = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [snaps, setSnaps] = useState([]);
    const [filteredSnaps, setFilteredSnaps] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [isSortedByLikes, setIsSortedByLikes] = useState(false);
    const categories = ["캐주얼", "스트릿", "빈티지", "모던", "댄디", "스포티", "꾸안꾸", "꾸꾸꾸"];

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => setUserInfo(res.data))
            .catch(error => {
                console.error("Error:", error);
                alert('유저 정보를 불러오지 못했습니다.');
                navigate('/');
            });

        fetchSnaps();
    }, [navigate]);

    const fetchSnaps = async () => {
        try {
            const res = await axiosInstance.get('/snaps');
            console.log(res.data)
            const snapsWithDetails = await Promise.all(
                res.data.map(async (snap) => {
                    const [likeCountRes, likedRes] = await Promise.all([
                        axiosInstance.get(`/snaps/${snap.snapId}/like-count`),
                        axiosInstance.get(`/snaps/${snap.snapId}/liked`, { withCredentials: true }),
                    ]);
                    return {
                        ...snap,
                        likeCount: likeCountRes.data.likeCount,
                        liked: likedRes.data.liked,
                    };
                })
            );
            const sortedSnaps = snapsWithDetails.sort((a, b) => new Date(b.snapCreatedDate) - new Date(a.snapCreatedDate));
            setSnaps(sortedSnaps);
            setFilteredSnaps(sortedSnaps);
        } catch (error) {
            console.error("Error fetching snaps:", error);
        }
    };

    const handleSnapClick = (snapId) => {
        navigate(`/detail_snap/${snapId}`);
    };

    const handleCategoryFilter = (category) => {
        setSelectedCategory(category);
        if (category) {
            setFilteredSnaps(snaps.filter(snap => snap.categories?.includes(category)));
        } else {
            setFilteredSnaps(snaps);
        }
    };

    const handleSortByLikes = () => {
        const sorted = [...filteredSnaps].sort((a, b) => b.likeCount - a.likeCount);
        setFilteredSnaps(isSortedByLikes ? snaps : sorted);
        setIsSortedByLikes(!isSortedByLikes);
    };

    const toggleLike = async (snapId, currentLiked) => {
        try {
            if (currentLiked) {
                await axiosInstance.delete(`/snaps/${snapId}/like`, { withCredentials: true });
            } else {
                await axiosInstance.post(`/snaps/${snapId}/like`, {}, { withCredentials: true });
            }
            fetchSnaps(); // 업데이트된 데이터를 다시 가져옴
        } catch (error) {
            console.error("Error toggling like:", error);
        }
    };

    return (
        <div className="snap-list-container">
            <h1>스냅 리스트</h1>
            {userInfo ? (
                <>
                    <p>안녕하세요, {userInfo.nickname}님!</p>
                    <div className="profile-image-frame">
                        <img
                            src={userInfo.profileImageUrl}
                            alt="프로필"
                            className="profile-image"
                        />
                    </div>
                    <button onClick={() => navigate('/upload_snap')}>스냅 등록하기</button>
                    <div className="category-filter">
                        <button
                            onClick={() => handleCategoryFilter('')}
                            className={!selectedCategory ? 'active' : ''}
                        >
                            전체
                        </button>
                        {categories.map(category => (
                            <button
                                key={category}
                                onClick={() => handleCategoryFilter(category)}
                                className={selectedCategory === category ? 'active' : ''}
                            >
                                {category}
                            </button>
                        ))}
                    </div>
                    <button onClick={handleSortByLikes} className="sort-button">
                        {isSortedByLikes ? "최신순 정렬" : "좋아요순 정렬"}
                    </button>
                    <div className="snap-list">
                        {filteredSnaps.map(snap => (
                            <div key={snap.snapId} className="snap-item">
                                <img src={snap.snapImageUrls[0]} alt="대표 이미지" onClick={() => handleSnapClick(snap.snapId)} />
                                <div className="like-button-container">
                                    <button
                                        onClick={() => toggleLike(snap.snapId, snap.liked)}
                                        className="like-button"
                                    >
                                        {snap.liked ? "❤️" : "🤍"}
                                    </button>
                                    <p>좋아요: {snap.likeCount}개</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            ) : (
                <p>유저 정보를 불러오는 중...</p>
            )}
        </div>
    );
};

export default Snap;
