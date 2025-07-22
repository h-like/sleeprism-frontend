// src/services/postService.ts

// import { PostResponseDTO } from '../types'; // PostResponseDTO 타입을 정의해야 합니다.

const API_BASE_URL = 'http://localhost:8080'; // 실제 백엔드 주소

const getAuthHeaders = () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) throw new Error("No auth token found");
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
};

// 내가 쓴 글 목록 가져오기
export const getMyPosts = async (): Promise<PostResponseDTO[]> => {
    const response = await fetch(`${API_BASE_URL}/api/me/posts`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch my posts");
    return response.json();
};

// 좋아요한 글 목록 가져오기
export const getLikedPosts = async (): Promise<PostResponseDTO[]> => {
    const response = await fetch(`${API_BASE_URL}/api/me/liked-posts`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch liked posts");
    return response.json();
};

// 북마크한 글 목록 가져오기
export const getBookmarkedPosts = async (): Promise<PostResponseDTO[]> => {
    const response = await fetch(`${API_BASE_URL}/api/me/bookmarked-posts`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch bookmarked posts");
    return response.json();
};