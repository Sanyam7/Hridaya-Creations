import { createContext, useContext, useState, useCallback } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Seed a demo user so login works out of the box
  const [users, setUsers] = useState([
    { id: 1, name: "Demo User", email: "demo@hridaya.com", password: "demo123", avatar: "D" }
  ]);
  const [currentUser, setCurrentUser] = useState(null);
  const [authError, setAuthError]     = useState("");

  const clearError = () => setAuthError("");

  const signup = useCallback(({ name, email, password }) => {
    if (users.find(u => u.email.toLowerCase() === email.toLowerCase())) {
      setAuthError("An account with this email already exists.");
      return false;
    }
    const newUser = { id: Date.now(), name, email, password, avatar: name[0].toUpperCase() };
    setUsers(prev => [...prev, newUser]);
    setCurrentUser(newUser);
    setAuthError("");
    return true;
  }, [users]);

  const login = useCallback(({ email, password }) => {
    const user = users.find(
      u => u.email.toLowerCase() === email.toLowerCase() && u.password === password
    );
    if (!user) {
      setAuthError("Invalid email or password.");
      return false;
    }
    setCurrentUser(user);
    setAuthError("");
    return true;
  }, [users]);

  const logout = useCallback(() => {
    setCurrentUser(null);
    setAuthError("");
  }, []);

  return (
    <AuthContext.Provider value={{ currentUser, authError, clearError, signup, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
