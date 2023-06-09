import axios from "axios";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const api = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

export const setAuthConfig = <T,>(token: string, params: T) => {
  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    params, //{},
  };

  return config;
};

export default api;
