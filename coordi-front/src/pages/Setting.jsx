import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../api/axios.jsx";
import "../css/Setting.css";

const Setting = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [editingField, setEditingField] = useState(null);
    const [editedValue, setEditedValue] = useState("");
    const [newProfileImage, setNewProfileImage] = useState(null);

    useEffect(() => {
        axiosInstance.get(`/member/me`, { withCredentials: true })
            .then(
                (res) => {
                    console.log(res.data)
                setUserInfo(res.data)})

            .catch((error) => {
                console.error("Error:", error);
                alert("유저 정보를 불러오지 못했습니다.");
                navigate("/");
            });
    }, [navigate]);

    const handleEditField = (field, currentValue) => {
        setEditingField(field);
        setEditedValue(currentValue);
    };

    const handleFileChange = (e) => {
        setNewProfileImage(e.target.files[0]);
    };

    const saveChanges = async () => {
        const formData = new FormData();
        if (editingField === "profileImage" && newProfileImage) {
            formData.append("profileImage", newProfileImage);
        } else {
            formData.append(editingField, editedValue);
        }

        try {
            await axiosInstance.put("/member/update", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            alert("수정되었습니다.");
            setEditingField(null);
            setNewProfileImage(null);
            setEditedValue("");
            const updatedUserInfo = await axiosInstance.get(`/member/me`, { withCredentials: true });
            setUserInfo(updatedUserInfo.data);
        } catch (error) {
            console.error("Error updating field:", error);
            alert("수정 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="setting-container">
            <h1>설정</h1>
            {userInfo && (
                <>
                    <div className="profile-section">
                        <div className="profile-image-container">
                            <img
                                src={userInfo.profileImageUrl}
                                alt="프로필 이미지"
                                className="profile-image"
                            />
                            <button onClick={() => handleEditField("profileImage")}>
                                프로필 이미지 변경
                            </button>
                        </div>
                        <div className="field">
                            <strong>닉네임:</strong> {userInfo.nickname}
                            <button onClick={() => handleEditField("nickname", userInfo.nickname)}>
                                수정
                            </button>
                        </div>
                        <div className="field">
                            <strong>나이:</strong> {userInfo.age}
                            <button onClick={() => handleEditField("age", userInfo.age)}>
                                수정
                            </button>
                        </div>
                        <div className="field">
                            <strong>성별:</strong> {userInfo.gender}
                            <button onClick={() => handleEditField("gender", userInfo.gender)}>
                                수정
                            </button>
                        </div>
                    </div>

                    {editingField && (
                        <div className="modal">
                            <div className="modal-content">
                                {editingField === "profileImage" ? (
                                    <>
                                        <h3>프로필 이미지 변경</h3>
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={handleFileChange}
                                        />
                                    </>
                                ) : (
                                    <>
                                        <h3>{editingField} 수정</h3>
                                        {editingField === "gender" ? (
                                            <select
                                                value={editedValue}
                                                onChange={(e) => setEditedValue(e.target.value)}
                                            >
                                                <option value="남성">남성</option>
                                                <option value="여성">여성</option>
                                            </select>
                                        ) : (
                                            <input
                                                type={editingField === "age" ? "number" : "text"}
                                                value={editedValue}
                                                onChange={(e) => setEditedValue(e.target.value)}
                                            />
                                        )}
                                    </>
                                )}
                                <div className="modal-buttons">
                                    <button onClick={saveChanges}>저장</button>
                                    <button onClick={() => setEditingField(null)}>취소</button>
                                </div>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default Setting;
