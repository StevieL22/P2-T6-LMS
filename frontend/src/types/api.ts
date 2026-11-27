export interface ApiErrorResponse {
    message?: string;
    error?: string;
    detail?: string;
    errors?: string[] | Record<string, string>;
}