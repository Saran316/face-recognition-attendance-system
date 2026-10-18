
import { apiRequest } from "./client.js";

export async function loginUser(email, password) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}
