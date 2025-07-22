import React, { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        // URL에서 'token'이라는 이름의 파라미터를 추출합니다.
        // 백엔드의 OAuth2AuthenticationSuccessHandler에서 이 이름으로 토큰을 보내줘야 합니다.
        const accessToken = searchParams.get('token'); 
        
        // 만약 다른 이름(예: 'accessToken')으로 온다면 아래와 같이 수정
        // const accessToken = searchParams.get('accessToken');

        if (accessToken) {
            console.log("소셜 로그인 성공. JWT Token:", accessToken);
            // 받은 토큰을 로컬 스토리지에 저장합니다.
            localStorage.setItem('jwtToken', accessToken);
            
            // 로그인 성공 후 사용자를 이동시킬 페이지로 리디렉션합니다.
            alert('소셜 로그인이 완료되었습니다.');
            navigate('/posts', { replace: true });
        } else {
            console.error("소셜 로그인 리디렉션 후 토큰을 찾을 수 없습니다.");
            // 토큰이 없는 경우, 에러 메시지와 함께 로그인 페이지로 돌려보냅니다.
            alert('소셜 로그인에 실패했습니다. 다시 시도해주세요.');
            navigate('/login', { replace: true });
        }
    }, [navigate, searchParams]);

    // 리디렉션 중 사용자에게 보여줄 UI
    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <h2>로그인 처리 중입니다. 잠시만 기다려주세요...</h2>
        </div>
    );
};

export default OAuth2RedirectHandler;