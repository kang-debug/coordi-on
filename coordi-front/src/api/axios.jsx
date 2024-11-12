import axios from "axios";

const instance = axios.create({
    baseURL: "http://localhost:8080/api",
    withCredentials: true, // 쿠키와 함께 전송
});

export default instance;