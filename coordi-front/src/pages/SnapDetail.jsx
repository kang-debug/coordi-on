import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from "../api/axios.jsx";
import '../css/SnapDetail.css';

const SnapDetail = () => {
    const { snapId } = useParams();
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [snap, setSnap] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [liked, setLiked] = useState(false);
    const [likeCount, setLikeCount] = useState(0);
    const [editingComment, setEditingComment] = useState(null);
    const [editedContent, setEditedContent] = useState('');

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(res => {
                setUserInfo(res.data);
                console.log(res.data);
            })
            .catch(error => {
                console.error("Error:", error);
                alert('유저 정보를 불러오지 못했습니다.');
                navigate('/');
            });

        fetchSnapDetails();
        fetchComments();
    }, [navigate, snapId]);

    useEffect(() => {
        if (userInfo) {
            checkIfLiked();
            fetchLikeCount();
        }
    }, [userInfo]);

    const fetchSnapDetails = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/${snapId}`);
            setSnap(res.data);
            console.log(res.data);
        } catch (error) {
            console.error("Error fetching snap details:", error);
        }
    };

    const fetchComments = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/${snapId}/comments`);
            setComments(res.data);
            console.log(res.data);
        } catch (error) {
            console.error("Error fetching comments:", error);
        }
    };

    const checkIfLiked = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/${snapId}/liked`, { withCredentials: true });
            setLiked(res.data.liked);
        } catch (error) {
            console.error("Error checking like status:", error);
        }
    };

    const fetchLikeCount = async () => {
        try {
            const res = await axiosInstance.get(`/snaps/${snapId}/like-count`);
            setLikeCount(res.data.likeCount);
        } catch (error) {
            console.error("Error fetching like count:", error);
        }
    };

    const handleLike = async () => {
        try {
            if (liked) {
                await axiosInstance.delete(`/snaps/${snapId}/like`, { withCredentials: true });
                setLiked(false);
                setLikeCount(likeCount - 1);
            } else {
                await axiosInstance.post(`/snaps/${snapId}/like`, {}, { withCredentials: true });
                setLiked(true);
                setLikeCount(likeCount + 1);
            }
        } catch (error) {
            console.error("Error toggling like:", error);
        }
    };

    const handleAddComment = async () => {
        if (!newComment.trim()) return;

        try {
            const res = await axiosInstance.post(
                `/snaps/${snapId}/comments`,
                { content: newComment },
                { withCredentials: true }
            );
            setComments([...comments, res.data]);
            setNewComment('');
        } catch (error) {
            console.error("Error adding comment:", error);
        }
    };

    const handleDeleteComment = async (commentId) => {
        const confirmDelete = window.confirm("댓글을 삭제하시겠습니까?");
        if (!confirmDelete) return;

        try {
            await axiosInstance.delete(`/snaps/${snapId}/comments/${commentId}`);
            setComments(comments.filter((comment) => comment.commentId !== commentId));
        } catch (error) {
            console.error("Error deleting comment:", error);
        }
    };

    const handleEditComment = (comment) => {
        setEditingComment(comment);
        setEditedContent(comment.content);
    };

    const handleSaveEdit = async () => {
        try {
            await axiosInstance.put(`/snaps/${snapId}/comments/${editingComment.commentId}`, {
                content: editedContent,
            });
            setComments(comments.map((comment) =>
                comment.commentId === editingComment.commentId
                    ? { ...comment, content: editedContent }
                    : comment
            ));
            setEditingComment(null);
            setEditedContent('');
        } catch (error) {
            console.error("Error editing comment:", error);
        }
    };

    const handleDeleteSnap = async () => {
        const confirmDelete = window.confirm("정말로 삭제하시겠습니까?");
        if (!confirmDelete) return;

        try {
            await axiosInstance.delete(`/snaps/${snapId}`);
            alert("게시물이 삭제되었습니다.");
            navigate("/snap");
        } catch (error) {
            console.error("Error deleting snap:", error);
            alert("게시물을 삭제하는 중 오류가 발생했습니다.");
        }
    };

    const handleEditSnap = () => {
        navigate(`/edit_snap/${snapId}`);
    };

    return (
        <div className="snap-detail-container">
            {userInfo && (
                <div className="user-info">
                    <p>안녕하세요, {userInfo.nickname}님!</p>
                </div>
            )}
            {snap ? (
                <>
                    <div className="snap-header">
                        <h2>{snap.author.nickname}</h2>
                        <p>{new Date(snap.snapCreatedDate).toLocaleDateString('ko-KR', {
                            month: '2-digit',
                            day: '2-digit',
                        })}</p>
                        {userInfo && userInfo.nickname === snap.author.nickname && (
                            <div className="action-buttons">
                                <button onClick={handleEditSnap}>수정</button>
                                <button onClick={handleDeleteSnap} className="delete-button">삭제</button>
                            </div>
                        )}
                    </div>
                    <div className="snap-images">
                        <div className="image-gallery">
                            {snap.snapImageUrls.map((url, index) => (
                                <img key={index} src={url} alt={`Snap ${index + 1}`} className="gallery-image" />
                            ))}
                        </div>
                    </div>
                    <div className="snap-description">
                        <p>{snap.snapDescription || ""}</p>
                    </div>
                    <div className="snap-actions">
                        <button onClick={handleLike}>
                            {liked ? "❤️" : "🤍"}
                        </button>
                        <p>좋아요: {likeCount}개</p>
                    </div>
                    <div className="comments-section">
                        <h3>댓글</h3>
                        <div className="add-comment">
                            <input
                                type="text"
                                placeholder="댓글을 입력하세요."
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                            />
                            <button onClick={handleAddComment}>등록</button>
                        </div>
                        <ul>
                            {comments.map((comment) => (
                                <li key={comment.commentId}>
                                    <p><strong>{comment.member.nickname}:</strong> {comment.content}</p>
                                    {userInfo && userInfo.nickname === comment.member.nickname && (
                                        <div>
                                            <button onClick={() => handleEditComment(comment)}>수정</button>
                                            <button onClick={() => handleDeleteComment(comment.commentId)}>삭제</button>
                                        </div>
                                    )}
                                </li>
                            ))}
                        </ul>
                    </div>
                    {editingComment && (
                        <div className="modal">
                            <div className="modal-content">
                                <textarea
                                    value={editedContent}
                                    onChange={(e) => setEditedContent(e.target.value)}
                                />
                                <button onClick={handleSaveEdit}>저장</button>
                                <button onClick={() => setEditingComment(null)}>취소</button>
                            </div>
                        </div>
                    )}
                </>
            ) : (
                <p>게시물을 불러오는 중입니다...</p>
            )}
            <button onClick={() => navigate(-1)}>뒤로가기</button>
        </div>
    );
};

export default SnapDetail;
