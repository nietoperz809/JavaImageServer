package inetserver.videostream;

class RangeCalculator {
    public int start;
    public int end;

    public RangeCalculator(String in, int filesize) {
        in = in.substring(in.indexOf("=") + 1);
        String[] parts = in.split("-");
        int st, en;
        try {
            st = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            st = -1;
        }
        try {
            en = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            en = -1;
        }
        start = st == -1 ? 0 : st;
        end = en == -1 ? filesize - 1 : en;
        if (st != -1 && en == -1) {
            start = st;
            end = filesize - 1;
        }
        if (st == -1 && en != -1) {
            start = filesize - en;
            end = filesize - 1;
        }
    }

    @Override
    public String toString() {
        return "Range{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
