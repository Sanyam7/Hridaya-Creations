import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { authApi, mapUser, tokenStore, ApiError } from "../api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [authError, setAuthError] = useState("");
  const [loading, setLoading] = useState(true); // true until session is restored

  const clearError = useCallback(() => setAuthError(""), []);

  // Restore an existing session on first load (if a token is present).
  useEffect(() => {
    let active = true;
    (async () => {
      if (!tokenStore.access) {
        setLoading(false);
        return;
      }
      try {
        const me = await authApi.me();
        if (active) setCurrentUser(mapUser(me));
      } catch {
        tokenStore.clear(); // expired/invalid
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const handleAuthResult = (auth) => {
    tokenStore.set({
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
    });
    setCurrentUser(mapUser(auth.user));
  };

  /** Register a new account, then sign the user in. */
  const signup = useCallback(async ({ name, email, password, mobileNumber }) => {
    setAuthError("");
    const parts = (name || "").trim().split(/\s+/);
    const firstName = parts[0] || "User";
    const lastName = parts.slice(1).join(" ") || firstName;
    try {
      await authApi.register({
        firstName,
        lastName,
        email,
        password,
        confirmPassword: password,
        mobileNumber,
      });
      // Registration succeeded — log in to obtain tokens.
      const auth = await authApi.login({ email, password });
      handleAuthResult(auth);
      return true;
    } catch (e) {
      setAuthError(fieldMessage(e) || "Could not create your account. Please try again.");
      return false;
    }
  }, []);

  const login = useCallback(async ({ email, password }) => {
    setAuthError("");
    try {
      const auth = await authApi.login({ email, password });
      handleAuthResult(auth);
      return true;
    } catch (e) {
      setAuthError(
        e instanceof ApiError && e.status === 401
          ? "Invalid email or password."
          : fieldMessage(e) || "Login failed. Please try again."
      );
      return false;
    }
  }, []);

  const logout = useCallback(() => {
    authApi.logout().catch(() => {}); // best-effort server-side revoke
    tokenStore.clear();
    setCurrentUser(null);
    setAuthError("");
  }, []);

  return (
    <AuthContext.Provider
      value={{ currentUser, authError, loading, clearError, signup, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

/** Prefer the first field-level validation message when present. */
function fieldMessage(e) {
  if (e instanceof ApiError && e.errors && e.errors.length) {
    return e.errors[0].message || e.message;
  }
  return e?.message;
}

export const useAuth = () => useContext(AuthContext);
