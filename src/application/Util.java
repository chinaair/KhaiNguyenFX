package application;

import java.util.UUID;

public class Util {
	
	// array de 64+2 digitos
	private final static char[] DIGITS62 = {'0','1','2','3','4','5','6','7','8','9',        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
	    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public static String generateString()
	{
		UUID u = UUID.randomUUID();
		return toIDString(u.getMostSignificantBits()) + toIDString(u.getLeastSignificantBits());
	}
	
	private static String toIDString(long i) {
	      char[] buf = new char[32];
	      int z = 60; // 1 << 6;
	      int cp = 32;
	      long b = z - 1;
	      do {
	          buf[--cp] = DIGITS62[(int)(i & b)];
	          i >>>= 6;
	      } while (i != 0);
	      return new String(buf, cp, (32-cp));
	  }
	

	public static boolean isNumeric(String string) {
	    if (string == null || string.isEmpty()) {
	        return false;
	    }
	    int i = 0;
	    int stringLength = string.length();
	    if (string.charAt(0) == '-') {
	        if (stringLength > 1) {
	            i++;
	        } else {
	            return false;
	        }
	    }
	    if (!Character.isDigit(string.charAt(i))
	            || !Character.isDigit(string.charAt(stringLength - 1))) {
	        return false;
	    }
	    i++;
	    stringLength--;
	    if (i >= stringLength) {
	        return true;
	    }
	    for (; i < stringLength; i++) {
	        if (!Character.isDigit(string.charAt(i))
	                && string.charAt(i) != '.') {
	            return false;
	        }
	    }
	    return true;
	}

	private static String moneyToText(String Number) {
		String sNumber = "";
		int len = Number.length();
		if (len == 1) {
			int iNu = Integer.parseInt("" + Number.charAt(0));
			sNumber += numberToTextA(iNu);
		} else if (len == 2) {
			int iChuc = Integer.parseInt("" + Number.charAt(0));
			int iDV = Integer.parseInt("" + Number.charAt(1));
			if (iChuc == 1) {
				if (iDV > 0) {
					sNumber += "Mười " + numberToTextA(iDV);
				} else {
					sNumber += "Mười ";
				}
			} else {
				sNumber += numberToTextA(iChuc) + " mươi " + numberToTextA(iDV);
			}
		} else {
			int iTram = Integer.parseInt("" + Number.charAt(0));
			int iChuc = Integer.parseInt("" + Number.charAt(1));
			int iDV = Integer.parseInt("" + Number.charAt(2));

			if (iChuc == 0) {
				if (iDV > 0) {
					sNumber += numberToTextA(iTram) + " trăm linh "
							+ numberToTextA(iDV);
				} else {
					sNumber += numberToTextA(iTram) + " trăm";
				}
			} else if (iChuc == 1) {
				if (iDV > 0) {
					sNumber += numberToTextA(iTram) + " trăm mười "
							+ numberToTextA(iDV);
				} else {
					sNumber += numberToTextA(iTram) + " trăm mười ";
				}
			} else {
				if (iDV > 0) {
					sNumber += numberToTextA(iTram) + " trăm "
							+ numberToTextA(iChuc) + " mươi "
							+ numberToTextA(iDV);
				} else {
					sNumber += numberToTextA(iTram) + " trăm "
							+ numberToTextA(iChuc) + " mươi ";
				}
			}
		}
		return sNumber;
	}

	public static String tranlate(String sNumber) {

		String sR = "";
		String sR1 = "";
		String sR2 = "";
		String sR3 = "";
		String sR4 = "";
		// sR = ChuyenDV(sNumber);

		int seq = 0;
		int k = 1;
		for (int i = sNumber.length(); i >= 0; i--) {
			if (seq == 3) {
				String subStr = sNumber.substring(i, i + seq);
				if (k == 1) {
					if(sNumber.length() > 3) {//tu hang nghin tro len
						sR = " đồng";
					} else {
						sR = moneyToText(subStr) + " đồng";
					}
				} else if (k == 2) {
					sR1 = moneyToText(subStr) + " nghìn ";
				} else if (k == 3) {
					sR2 = moneyToText(subStr) + " triệu ";
				} else {
					sR3 = moneyToText(subStr) + " tỷ ";
				}
				seq = 0;
				k++;
			}
			seq++;
		}
		if (seq > 1) {
			String subStr = sNumber.substring(0, seq - 1);
			if (k == 1) {
				sR = moneyToText(subStr) + " đồng";
			} else if (k == 2) {
				sR1 = moneyToText(subStr) + " nghìn ";
			} else if (k == 3) {
				sR2 = moneyToText(subStr) + " triệu ";
			} else {
				sR3 = moneyToText(subStr) + " tỷ ";
			}
		}
		// seq
		sR4 = sR3 + sR2 + sR1 + sR;

		return sR4;

	}
		
	private static String numberToTextA(int number) {
		String sR = "";
		switch (number) {
		case 0:
			sR = "không";
			break;
		case 1:
			sR = "một";
			break;
		case 2:
			sR = "hai";
			break;
		case 3:
			sR = "ba";
			break;
		case 4:
			sR = "bốn";
			break;
		case 5:
			sR = "năm";
			break;
		case 6:
			sR = "sáu";
			break;
		case 7:
			sR = "bảy";
			break;
		case 8:
			sR = "tám";
			break;
		case 9:
			sR = "chín";
			break;
		default:
			sR = "";
		}
		return sR;
	}
	
	public static String leftPadStringWithChar(String s, int fixedLength, char c){
	    if(fixedLength < s.length()){
	        throw new IllegalArgumentException();
	    }

	    StringBuilder sb = new StringBuilder(s);

	    for(int i = 0; i < fixedLength - s.length(); i++){
	        sb.insert(0, c);
	    }

	    return sb.toString();
	}
	 

}
