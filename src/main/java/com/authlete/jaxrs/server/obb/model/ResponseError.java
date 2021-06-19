/*
 * Copyright (C) 2021 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.obb.model;


import java.io.Serializable;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


/**
 * ResponseError
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_ResponseError"
 *      >ResponseError</a>
 */
public class ResponseError implements Serializable
{
    private static final long serialVersionUID = 1L;


    private Error[] errors;
    private Meta meta;


    public ResponseError()
    {
    }


    public ResponseError(Error[] errors, Meta meta)
    {
        this.errors = errors;
        this.meta   = meta;
    }


    public Error[] getErrors()
    {
        return errors;
    }


    public ResponseError setErrors(Error[] errors)
    {
        this.errors = errors;

        return this;
    }


    public Meta getMeta()
    {
        return meta;
    }


    public ResponseError setMeta(Meta meta)
    {
        this.meta = meta;

        return this;
    }


    public static ResponseError create(
            String code, String title, String detail)
    {
        Error[] errors = new Error[] { new Error(code, title, detail) };
        Meta    meta   = new Meta(1, 1, ObbUtils.formatNow());

        return new ResponseError(errors, meta);
    }
}
