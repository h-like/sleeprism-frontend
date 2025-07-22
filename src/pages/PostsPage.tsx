import React from 'react';

const PostsPage = () => {
    const handleLogout = () => {
        localStorage.removeItem('jwtToken');
        window.location.href = '/login';
        alert('로그아웃 되었습니다.');
    };

    return (
        <div style={{ padding: '20px' }}>
            <h1>로그인 성공!</h1>
            <p>메인 컨텐츠 페이지입니다.</p>
            <button onClick={handleLogout} style={{ padding: '10px 20px', cursor: 'pointer' }}>
                로그아웃
            </button>
        </div>
    );
};

export default PostsPage;