// src/pages/myPage/PostListPage.tsx

import React, { useEffect, useState } from 'react';
import { SplinePointer } from 'lucide-react'; // 로딩 아이콘
import { ErrorDisplay } from './MyPageUI'; // 에러 표시용 UI 컴포넌트 (기존에 사용하시던 것)
import { getBookmarkedPosts, getLikedPosts, getMyPosts } from '../../service/postService';
import type { Post } from '../../service/apiService';

// --- 재사용 가능한 게시글 목록 UI ---
interface PostListProps {
    posts: Post[];
}

const PostList: React.FC<PostListProps> = ({ posts }) => {
    if (posts.length === 0) {
        return <div className="no-content">게시글이 없습니다.</div>;
    }

    return (
        <ul className="post-list">
            {posts.map(post => (
                <li key={post.id} className="post-list-item">
                    <span className="post-title">{post.title}</span>
                    <span className="post-meta">
                        {post.authorNickname} · {new Date(post.createdAt).toLocaleDateString()}
                    </span>
                </li>
            ))}
        </ul>
    );
};


// --- 데이터를 가져와서 목록을 보여주는 범용 페이지 컴포넌트 ---
interface PostListPageProps {
    title: string;
    fetcher: () => Promise<Post[]>; // 데이터를 가져올 API 함수
}

const PostListPage: React.FC<PostListPageProps> = ({ title, fetcher }) => {
    const [posts, setPosts] = useState<Post[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadPosts = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const data = await fetcher();
                setPosts(data);
            } catch (err: any) {
                setError(err.message || '데이터를 불러오는 데 실패했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        loadPosts();
    }, [fetcher]); // fetcher 함수가 바뀔 때마다 다시 데이터를 로드합니다.

    return (
        <div className="page-content">
            <header className="page-header">
                <h1>{title}</h1>
            </header>
            {isLoading && <SplinePointer />}
            {error && <ErrorDisplay message={error} />}
            {!isLoading && !error && <PostList posts={posts} />}
        </div>
    );
};

// --- 각 페이지를 정의하는 컴포넌트들 ---

// import { getMyPosts, getLikedPosts, getBookmarkedPosts } from '../../services/apiService';

export const MyPostsPage = () => (
    <PostListPage title="내가 쓴 글" fetcher={getMyPosts} />
);

export const LikedPostsPage = () => (
    <PostListPage title="좋아요한 글" fetcher={getLikedPosts} />
);

export const BookmarkedPostsPage = () => (
    <PostListPage title="북마크" fetcher={getBookmarkedPosts} />
);
