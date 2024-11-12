import React from 'react';
import './App.css'
import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import Login from './pages/Login.jsx';
import SignUp from './pages/SignUp.jsx';
import Home from './pages/Home.jsx';
import KakaoHandler from './pages/KakaoHandler.jsx';
import SignUpExternal from "./pages/SignUpExternal.jsx";
import FindPass from "./pages/FindPass.jsx";

function App() {
    return (
            <div className="App-container">
                <Router>
                    <Routes>
                        <Route path="/" element={<Login/>}/>
                        <Route path="/auth/kakao" element={<KakaoHandler/>}/>
                        <Route path="/signup" element={<SignUp/>}/>
                        <Route path="/signupex" element={<SignUpExternal/>}/>
                        <Route path="/findpass" element={<FindPass/>}/>
                        <Route path="/home" element={<Home/>}/>
                    </Routes>
                </Router>
            </div>

    );
}

export default App;
