import { createContext, useContext, useEffect, useState } from "react";

// --- 데이터 타입 정의 ---
interface UserProfile {
  profileImageUrl: string;
  nickname: string;
  email: string;
  age: number;
  gender: string;
  createdAt: string;
  userId: string;
  postCount: number;
  likedPostCount: number;
  bookmarkedPostCount: number;
}

// --- API 함수 ---

/**
 * JWT 토큰을 사용하여 서버로부터 사용자 프로필 정보를 가져옵니다.
 * @returns {Promise<UserProfile>} 사용자 프로필 데이터
 */
const fetchUserProfile = async (): Promise<UserProfile> => {
  // localStorage에서 JWT 토큰을 가져옵니다.
  const token = localStorage.getItem('jwtToken');

  // 토큰이 없으면 인증되지 않은 사용자로 간주하고 에러를 발생시킵니다.
  if (!token) {
    throw new Error('인증 토큰이 없습니다. 로그인이 필요합니다.');
  }

  console.log("JWT 토큰으로 프로필 정보 요청 중...");

  // 백엔드 API에 GET 요청을 보냅니다.
  // 'Authorization' 헤더에 Bearer 토큰을 담아 보냅니다.
  const response = await fetch('/api/users/profile', { // 실제 백엔드 엔드포인트로 변경해야 합니다.
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
  });

  // 응답이 성공적이지 않으면 에러를 처리합니다.
  if (!response.ok) {
    // 401 Unauthorized, 403 Forbidden 등의 경우 토큰이 유효하지 않음을 의미할 수 있습니다.
    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('jwtToken'); // 잘못된 토큰은 삭제
    }
    throw new Error(`프로필 정보를 불러오는 데 실패했습니다. 상태 코드: ${response.status}`);
  }

  // 응답 데이터를 JSON 형태로 파싱하여 반환합니다.
  const userData: UserProfile = await response.json();
  return userData;
};

export const updateUserProfile = async (formData: FormData): Promise<void> => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        throw new Error('인증 토큰이 없습니다. 로그인이 필요합니다.');
    }

    console.log("프로필 업데이트 요청:", formData);
    
    // 백엔드 API에 PUT 또는 PATCH 요청을 보냅니다.
    const response = await fetch('/api/users/profile', { // 실제 백엔드 엔드포인트로 변경해야 합니다.
        method: 'PUT', // 또는 'PUT'
        headers: {
            // FormData를 보낼 때는 'Content-Type'을 명시하지 않는 것이 좋습니다.
            // 브라우저가 자동으로 'multipart/form-data'와 함께 boundary를 설정해줍니다.
            'Authorization': `Bearer ${token}`
        },
        body: formData,
    });

    if (!response.ok) {
        // 서버에서 에러 메시지를 보냈을 경우를 대비해 text()로 읽어옵니다.
        const errorMessage = await response.text();
        throw new Error(errorMessage || `프로필 업데이트에 실패했습니다. 상태 코드: ${response.status}`);
    }

    // 성공 응답(e.g., 200 OK or 204 No Content)을 받으면 아무것도 반환하지 않고 종료됩니다.
};


// --- 사용자 데이터 컨텍스트 (Context API) ---
interface UserContextType {
  user: UserProfile | null;
  isLoggedIn: boolean;
  isLoading: boolean;
  error: string | null;
  login: (token: string) => void;
  logout: () => void;
  refetch: () => void;
  updateUser: (updatedData: Partial<UserProfile>) => void;
}

const UserContext = createContext<UserContextType | null>(null);

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) throw new Error('useUser must be used within a UserProvider');
  return context;
};

export const UserProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const loadUser = async () => {
    // 토큰이 있는지 먼저 확인합니다. 없으면 로딩할 필요가 없습니다.
    if (!localStorage.getItem('jwtToken')) {
        setIsLoading(false);
        setIsLoggedIn(false);
        setUser(null);
        return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const userData = await fetchUserProfile();
      setUser(userData);
      setIsLoggedIn(true);
    } catch (err: any) {
      setError(err.message || "프로필 정보를 불러오는 데 실패했습니다.");
      setUser(null);
      setIsLoggedIn(false);
    } finally {
      setIsLoading(false);
    }
  };

  // 컴포넌트 마운트 시 토큰이 있으면 사용자 정보를 불러옵니다.
  useEffect(() => {
    loadUser();
  }, []);
  
  // 클라이언트 상태를 직접 업데이트하기 위한 함수 (예: 옵티미스틱 UI)
  const updateUser = (updatedData: Partial<UserProfile>) => {
    setUser(prevUser => prevUser ? { ...prevUser, ...updatedData } : null);
  };

  // 로그인 함수: 토큰을 저장하고 사용자 정보를 불러옵니다.
  const login = (token: string) => {
    localStorage.setItem('jwtToken', token);
    loadUser();
  };

  // 로그아웃 함수: 토큰을 삭제하고 상태를 초기화합니다.
  const logout = () => {
    localStorage.removeItem('jwtToken');
    setUser(null);
    setIsLoggedIn(false);
    setError(null);
  };

  const value = { 
    user, 
    isLoggedIn,
    isLoading, 
    error, 
    login,
    logout,
    refetch: loadUser, 
    updateUser 
  };

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};