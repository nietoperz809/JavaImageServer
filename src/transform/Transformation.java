/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author Administrator
 */
public interface Transformation
{
    String utf8 = StandardCharsets.UTF_8.name();
    String transform (String in);
    String retransform (String in);
}
