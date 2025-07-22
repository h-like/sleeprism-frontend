// MyPage.tsx

import { Bookmark, Heart, Home, PenSquare, User } from "lucide-react";
import { NavLink, Outlet, Route, Routes, useLocation } from "react-router-dom";
import '../../public/css/MyPage.css'
import MyPageDashboard from "./myPage/MyPageDashboard";
import EditProfilePage from "./myPage/EditProfilePage";
import { BookmarkedPostsPage, LikedPostsPage, MyPostsPage } from "./myPage/MyPostsPage";
import { UserProvider, useUser } from "../contexts/UserContext";



// --- 레이아웃 컴포넌트 (MyPageLayout)는 수정할 필요가 없습니다. ---
const MyPageLayout = () => {
  const { user, isLoading, error } = useUser();
  const location = useLocation();

  return (
    <div className="mypage-container">
      <aside className="mypage-sidebar">
        {isLoading ? (
          <div className="profile-header-skeleton">
            <div className="avatar-skeleton"></div>
            <div className="text-skeleton"></div>
            <div className="text-skeleton short"></div>
          </div>
        ) : error || !user ? (
          <div className="profile-header-error">정보 로딩 실패</div>
        ) : (
          <div className="profile-header">
            <img src={user.profileImageUrl} alt="프로필" className="profile-avatar" />
            <h2 className="profile-name">{user.nickname}</h2>
            <p className="profile-email">{user.email}</p>
          </div>
        )}
        <nav>
          <NavLink to="" end><Home size={18} /> 대시보드</NavLink>
          <NavLink to="edit-profile"><User size={18} /> 개인 정보 수정</NavLink>
          <NavLink to="my-posts"><PenSquare size={18} /> 내가 쓴 글</NavLink>
          <NavLink to="liked-posts"><Heart size={18} /> 좋아요한 글</NavLink>
          <NavLink to="bookmarked-posts"><Bookmark size={18} /> 북마크</NavLink>
        </nav>
      </aside>
      <main className="mypage-main-content">
        <Outlet />
      </main>
    </div>
  );
};


export default function MyPage() {
    return (
        <UserProvider>
            <Routes>
                <Route path="/" element={<MyPageLayout />}>
                    <Route index element={<MyPageDashboard />} />
                    <Route path="edit-profile" element={<EditProfilePage />} />
                    {/* --- PlaceholderPage를 실제 페이지 컴포넌트로 교체합니다. --- */}
                    <Route path="my-posts" element={<MyPostsPage />} />
                    <Route path="liked-posts" element={<LikedPostsPage />} />
                    <Route path="bookmarked-posts" element={<BookmarkedPostsPage />} />
                </Route>
            </Routes>
        </UserProvider>
    );
}
