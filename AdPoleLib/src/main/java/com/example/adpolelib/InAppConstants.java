package com.example.adpolelib;

public class InAppConstants {

    protected enum RequestType {
        Impression,
        Conversion,
        Conversion_Retry
    }


    public enum AdType {
        BANNER(1),
        MOBILE_BANNER(0),
        INTERSTITIAL(2),
        NATIVE(3);

        private int typeCode;

        AdType(int typeCode){
            this.typeCode = typeCode;
        }

        public int getTypeCode() {
            return typeCode;
        }

        public static AdType getWithNumber(int num){
            if(num == 0)
                return MOBILE_BANNER;
            else if(num == 1)
                return BANNER;
            else if(num == 2)
                return NATIVE;
            return null;
        }
    }
}
