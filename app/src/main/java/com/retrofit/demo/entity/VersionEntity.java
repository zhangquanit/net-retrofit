package com.retrofit.demo.entity;

/**
 * 张全
 */

public class VersionEntity {

    /**
     * errorCode : 0
     * data : {"id":31,"url":"http://www.taobanapp.com/app/Android_uc.apk","description":"1.美丽计划让你轻松变美\n2.积分商城每天领大奖\n3.首页的UI变得更加美美哒~\n4.资讯内容更加丰富了\n5.更多福利，更多奖品等你来捡哦~","from":"Android_uc","version":"2.0.0","versionCode":20000,"forceUpdate":0}
     */

    private int errorCode;
    /**
     * id : 31
     * url : http://www.taobanapp.com/app/Android_uc.apk
     * description : 1.美丽计划让你轻松变美
     2.积分商城每天领大奖
     3.首页的UI变得更加美美哒~
     4.资讯内容更加丰富了
     5.更多福利，更多奖品等你来捡哦~
     * from : Android_uc
     * version : 2.0.0
     * versionCode : 20000
     * forceUpdate : 0
     */

    private DataBean data;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "VersionEntity{" +
                "errorCode=" + errorCode +
                ", data=" + data +
                '}';
    }

    public static class DataBean {
        private int id;
        private String url;
        private String description;
        private String from;
        private String version;
        private int versionCode;
        private int forceUpdate;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public int getForceUpdate() {
            return forceUpdate;
        }

        public void setForceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", url='" + url + '\'' +
                    ", description='" + description + '\'' +
                    ", from='" + from + '\'' +
                    ", version='" + version + '\'' +
                    ", versionCode=" + versionCode +
                    ", forceUpdate=" + forceUpdate +
                    '}';
        }
    }
}
