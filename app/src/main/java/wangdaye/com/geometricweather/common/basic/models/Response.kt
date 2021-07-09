package wangdaye.com.geometricweather.common.basic.models

class Response<T>(val result: T?,
                  val status: Status) {

    companion object {

        fun<T> success(result: T?): Response<T> {
            return Response(result, Status.SUCCEED)
        }

        fun<T> failure(result: T?): Response<T> {
            return Response(result, Status.FAILED)
        }
    }

    enum class Status {
        SUCCEED, FAILED
    }

    fun isSucceed() = status == Status.SUCCEED

    fun isFailed() = status == Status.FAILED
}