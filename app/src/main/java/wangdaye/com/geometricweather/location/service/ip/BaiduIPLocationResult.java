package wangdaye.com.geometricweather.location.service.ip;

public class BaiduIPLocationResult {

    /**
     * address : CN|北京|北京|None|CHINANET|1|None
     * content : {"address":"北京市","address_detail":{"city":"北京市","city_code":131,"district":"","province":"北京市","street":"","street_number":""},"point":{"x":"116.39564504","y":"39.92998578"}}
     * status : 0
     */

    private String address;
    private ContentBean content;
    private int status;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ContentBean getContent() {
        return content;
    }

    public void setContent(ContentBean content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class ContentBean {
        /**
         * address : 北京市
         * address_detail : {"city":"北京市","city_code":131,"district":"","province":"北京市","street":"","street_number":""}
         * point : {"x":"116.39564504","y":"39.92998578"}
         */

        private String address;
        private AddressDetailBean address_detail;
        private PointBean point;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public AddressDetailBean getAddress_detail() {
            return address_detail;
        }

        public void setAddress_detail(AddressDetailBean address_detail) {
            this.address_detail = address_detail;
        }

        public PointBean getPoint() {
            return point;
        }

        public void setPoint(PointBean point) {
            this.point = point;
        }

        public static class AddressDetailBean {
            /**
             * city : 北京市
             * city_code : 131
             * district :
             * province : 北京市
             * street :
             * street_number :
             */

            private String city;
            private int city_code;
            private String district;
            private String province;
            private String street;
            private String street_number;

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public int getCity_code() {
                return city_code;
            }

            public void setCity_code(int city_code) {
                this.city_code = city_code;
            }

            public String getDistrict() {
                return district;
            }

            public void setDistrict(String district) {
                this.district = district;
            }

            public String getProvince() {
                return province;
            }

            public void setProvince(String province) {
                this.province = province;
            }

            public String getStreet() {
                return street;
            }

            public void setStreet(String street) {
                this.street = street;
            }

            public String getStreet_number() {
                return street_number;
            }

            public void setStreet_number(String street_number) {
                this.street_number = street_number;
            }
        }

        public static class PointBean {
            /**
             * x : 116.39564504
             * y : 39.92998578
             */

            private String x;
            private String y;

            public String getX() {
                return x;
            }

            public void setX(String x) {
                this.x = x;
            }

            public String getY() {
                return y;
            }

            public void setY(String y) {
                this.y = y;
            }
        }
    }
}
