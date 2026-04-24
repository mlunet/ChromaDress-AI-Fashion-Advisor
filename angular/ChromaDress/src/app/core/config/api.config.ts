import { environment } from '../../../environments/environment';

export const API_CONFIG = {
  baseUrl: environment.apiUrl,
  auth: {
    base: '/auth',
    login: '/login',
    register: '/register',
    refresh: '/refresh',
    logout: '/logout',
  },
  analysis: {
    detect: '/analysis/detect',
  },
  clothing: {
    upload: '/clothing/upload',
    myCloset: '/clothing/my-closet',
    base: '/clothing',
  },
};
