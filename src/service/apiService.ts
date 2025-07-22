// src/services/apiService.ts

// 게시글 데이터의 타입을 정의합니다. 백엔드의 PostResponseDTO와 일치시켜야 합니다.
export interface Post {
    id: number;
    title: string;
    authorNickname: string;
    createdAt: string;
    viewCount: number;
    likeCount: number;
}

// 백엔드 서버의 기본 URL 주소입니다.
const API_BASE_URL = 'http://localhost:8080'; // 실제 백엔드 서버 주소로 변경하세요.

/**
 * 로컬 스토리지에서 JWT 토큰을 가져와 인증 헤더를 생성합니다.
 * @returns {HeadersInit} 인증 헤더
 */
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        // 토큰이 없는 경우, 로그인 페이지로 리디렉션하거나 에러 처리를 할 수 있습니다.
        throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
    }
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
};

/**
 * API 요청을 보내고 응답을 처리하는 헬퍼 함수입니다.
 * @param {string} endpoint API 엔드포인트
 * @returns {Promise<T>} API 응답 데이터
 */
async function fetchFromAPI<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        // 서버에서 보낸 에러 메시지를 포함하여 에러를 throw합니다.
        const errorData = await response.json().catch(() => ({ message: '알 수 없는 오류가 발생했습니다.' }));
        throw new Error(errorData.message || `API 요청 실패: ${response.status}`);
    }
    return response.json();
}

// --- "나의 활동" 관련 API 함수들 ---

/**
 * 현재 로그인한 사용자가 작성한 글 목록을 가져옵니다.
 * @returns {Promise<Post[]>} 게시글 목록
 */
export const getMyPosts = (): Promise<Post[]> => {
    return fetchFromAPI<Post[]>('/api/me/posts');
};

/**
 * 현재 로그인한 사용자가 좋아요를 누른 글 목록을 가져옵니다.
 * @returns {Promise<Post[]>} 게시글 목록
 */
export const getLikedPosts = (): Promise<Post[]> => {
    return fetchFromAPI<Post[]>('/api/me/liked-posts');
};

/**
 * 현재 로그인한 사용자가 북마크한 글 목록을 가져옵니다.
 * @returns {Promise<Post[]>} 게시글 목록
 */
export const getBookmarkedPosts = (): Promise<Post[]> => {
    return fetchFromAPI<Post[]>('/api/me/bookmarked-posts');
};
