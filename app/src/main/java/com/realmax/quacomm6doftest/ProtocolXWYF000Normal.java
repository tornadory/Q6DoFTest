package com.realmax.quacomm6doftest;

/**
 * Created by admin on 12/29/2017.
 */

public class ProtocolXWYF000Normal {
    public int type;
    public int id;
    public float yaw;
    public float pitch;
    public float roll;
    public int cnt;
    public int trigger;
    public int ioKeyBytes;
    public Boolean[] iokeys = new Boolean[8];
    public Boolean[] tp_events = new Boolean[4];
    public int touchpad_x;
    public int touchpad_y;

    public class sensor_xyz_t
    {
        public int x;
        public int y;
        public int z;
    }

    public long stamp;
    public sensor_xyz_t gyro = new sensor_xyz_t();
    public sensor_xyz_t accel = new sensor_xyz_t();
    public sensor_xyz_t magnet = new sensor_xyz_t();

    private static int byte_to_int(byte b)
    {
        return (((int)b)+256)%256;
    }

    private static int byte2_to_int(byte b15_8, byte b7_0)
    {
        int tmp = byte2_to_uint(b15_8, b7_0);
        if(tmp > 0x8000){
            tmp -= 0x10000;
        }
        return tmp;
    }
    private static int byte2_to_uint(byte b15_8, byte b7_0)
    {
        int tmp = byte_to_int(b7_0);
        tmp += byte_to_int(b15_8) << 8;
        return tmp;
    }

    private static int byte3_to_int(byte b23_16, byte b15_8, byte b7_0)
    {
        int tmp = byte_to_int(b7_0);
        tmp += byte_to_int(b15_8) << 8;
        tmp += byte_to_int(b23_16) << 16;
        if(tmp > 0x800000)
        {
            tmp -= 0x1000000;
        }
        return tmp;
    }

    private static long get_u64_from_bytes(byte[] dataIn, int index)
    {

        long tmp = 0;
        tmp += byte_to_int(dataIn[index+0]) << 56;
        tmp += byte_to_int(dataIn[index+1]) << 48;
        tmp += byte_to_int(dataIn[index+2]) << 40;
        tmp += byte_to_int(dataIn[index+3]) << 32;
        tmp += byte_to_int(dataIn[index+4]) << 24;
        tmp += byte_to_int(dataIn[index+5]) << 16;
        tmp += byte_to_int(dataIn[index+6]) <<  8;
        tmp += byte_to_int(dataIn[index+7]) <<  0;
        return tmp;
    }

    private sensor_xyz_t get_xyz_from_bytes(byte[] dataIn, int index)
    {
        sensor_xyz_t tmp = new sensor_xyz_t();
        tmp.x = byte2_to_int(dataIn[index+0], dataIn[index+1]);
        tmp.y = byte2_to_int(dataIn[index+2], dataIn[index+3]);
        tmp.z = byte2_to_int(dataIn[index+4], dataIn[index+5]);
        return tmp;
    }

    @Override
    public String toString()
    {
        String s = String.format("buttons=0x%x \r\n" +
                        "rotation:\r\nyaw:%05.2f,\r\npitch:%05.2f,\r\nroll:%05.2f",
                this.ioKeyBytes,
                this.yaw,
                this.pitch,
                this.roll
        );
        return s;
    }

    public String toJSON()
    {
        //"{\"position\":{ \"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f},  \"rotation\": {\"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f, \"w\":%0.2f}}"
        //String s = String.format("{\"buttons\": \"%x\", \"rotation\":{\"yaw\": \"%05.2f\",\"pitch\":\"%05.2f\",\"roll\":\"%05.2f\"}}",
        String s = String.format("{\"buttons\": \"%x\", \"rotation\":{\"yaw\": \"%05.2f\",\"pitch\":\"%05.2f\",\"roll\":\"%05.2f\"}}",
                this.ioKeyBytes,
                this.yaw,
                this.pitch,
                this.roll
        );
        return s;
    }

    public ProtocolXWYF000Normal(byte[] dataIn)
    {
        if(dataIn.length < 20)
        {
            return;
        }

        type = ProtocolXWYF000Normal.byte_to_int(dataIn[0]);
        id = ProtocolXWYF000Normal.byte_to_int(dataIn[1]);
        yaw = ProtocolXWYF000Normal.byte3_to_int(dataIn[2], dataIn[3], dataIn[4])/10000.0f;
        pitch = ProtocolXWYF000Normal.byte3_to_int(dataIn[5], dataIn[6], dataIn[7])/10000.0f;
        roll = ProtocolXWYF000Normal.byte3_to_int(dataIn[8], dataIn[9], dataIn[10])/10000.0f;
        cnt = ProtocolXWYF000Normal.byte3_to_int(dataIn[11], dataIn[12], dataIn[13]);
        trigger = ProtocolXWYF000Normal.byte_to_int(dataIn[14]);
        ioKeyBytes = dataIn[15];
        int x = ProtocolXWYF000Normal.byte2_to_uint(dataIn[16], dataIn[17]);
        int y = ProtocolXWYF000Normal.byte2_to_uint(dataIn[18], dataIn[19]);

        int touchpad_events = (x>>14)&0x03;

        touchpad_x = x &0x1FFF;
        touchpad_y = y;

        if(dataIn.length >= 46)
        {
            stamp = ProtocolXWYF000Normal.get_u64_from_bytes(dataIn, 20);
            gyro = get_xyz_from_bytes(dataIn, 28);
            accel = get_xyz_from_bytes(dataIn, 34);
            magnet = get_xyz_from_bytes(dataIn, 40);
        }
    }
}
