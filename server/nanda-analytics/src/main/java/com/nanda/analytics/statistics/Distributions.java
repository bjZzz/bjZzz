package com.nanda.analytics.statistics;

/**
 * 轻量统计分布近似（无第三方依赖）。
 * 提供正态、t、卡方分布的尾概率近似，用于 p 值估计。
 */
public final class Distributions {

    private Distributions() {
    }

    /** 标准正态累积分布函数（Abramowitz & Stegun 7.1.26 近似 erf）。 */
    public static double normalCdf(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    public static double erf(double x) {
        double sign = x < 0 ? -1 : 1;
        double ax = Math.abs(x);
        double t = 1.0 / (1.0 + 0.3275911 * ax);
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t * Math.exp(-ax * ax);
        return sign * y;
    }

    /** 双侧正态 p 值。 */
    public static double twoTailedNormalP(double z) {
        return 2.0 * (1.0 - normalCdf(Math.abs(z)));
    }

    /**
     * t 分布双侧 p 值近似：对自由度做 Cornish-Fisher 风格校正后退化为正态尾概率。
     */
    public static double twoTailedTP(double t, double df) {
        if (df <= 0) {
            return Double.NaN;
        }
        double x = df / (df + t * t);
        double p = incompleteBeta(df / 2.0, 0.5, x);
        return Math.max(0.0, Math.min(1.0, p));
    }

    /**
     * 卡方分布上尾概率 P(X > x)（Wilson-Hilferty 立方根正态近似）。
     */
    public static double chiSquareUpperP(double x, double df) {
        if (x <= 0 || df <= 0) {
            return 1.0;
        }
        double t = Math.pow(x / df, 1.0 / 3.0);
        double mean = 1.0 - 2.0 / (9.0 * df);
        double sd = Math.sqrt(2.0 / (9.0 * df));
        double z = (t - mean) / sd;
        return 1.0 - normalCdf(z);
    }

    /** 正则化不完全 Beta 函数 I_x(a,b)（连分式法，用于 t 分布）。 */
    public static double incompleteBeta(double a, double b, double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        if (x >= 1.0) {
            return 1.0;
        }
        double lbeta = logGamma(a) + logGamma(b) - logGamma(a + b);
        double front = Math.exp(Math.log(x) * a + Math.log(1.0 - x) * b - lbeta) / a;
        double cf = betaContinuedFraction(a, b, x);
        return front * cf;
    }

    private static double betaContinuedFraction(double a, double b, double x) {
        int maxIterations = 200;
        double epsilon = 1e-12;
        double c = 1.0;
        double d = 1.0 - (a + b) * x / (a + 1.0);
        if (Math.abs(d) < epsilon) {
            d = epsilon;
        }
        d = 1.0 / d;
        double result = d;
        for (int i = 1; i <= maxIterations; i++) {
            int m = i / 2;
            double numerator;
            if (i % 2 == 0) {
                numerator = (m * (b - m) * x) / ((a + 2.0 * m - 1.0) * (a + 2.0 * m));
            } else {
                numerator = -((a + m) * (a + b + m) * x) / ((a + 2.0 * m) * (a + 2.0 * m + 1.0));
            }
            d = 1.0 + numerator * d;
            if (Math.abs(d) < epsilon) {
                d = epsilon;
            }
            d = 1.0 / d;
            c = 1.0 + numerator / c;
            if (Math.abs(c) < epsilon) {
                c = epsilon;
            }
            double cd = c * d;
            result *= cd;
            if (Math.abs(1.0 - cd) < epsilon) {
                break;
            }
        }
        return result;
    }

    /** Lanczos 近似的 log Gamma。 */
    public static double logGamma(double x) {
        double[] g = {
                676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012,
                9.9843695780195716e-6, 1.5056327351493116e-7
        };
        if (x < 0.5) {
            return Math.log(Math.PI / Math.sin(Math.PI * x)) - logGamma(1.0 - x);
        }
        x -= 1.0;
        double a = 0.99999999999980993;
        double t = x + 7.5;
        for (int i = 0; i < g.length; i++) {
            a += g[i] / (x + i + 1);
        }
        return 0.5 * Math.log(2 * Math.PI) + (x + 0.5) * Math.log(t) - t + Math.log(a);
    }
}
