use axum::response::{IntoResponse, Response};

#[derive(Debug)]
pub struct ResponseError<T: ToString> {
    status: hyper::StatusCode,
    message: T,
}

impl<T> From<(hyper::StatusCode, T)> for ResponseError<T>
where
    T: ToString,
{
    fn from(value: (hyper::StatusCode, T)) -> Self {
        let (status, message) = value;

        ResponseError { status, message }
    }
}

impl<T> IntoResponse for ResponseError<T>
where
    T: ToString,
{
    fn into_response(self) -> Response {
        let ResponseError { status, message } = self;
        (status, message.to_string()).into_response()
    }
}
