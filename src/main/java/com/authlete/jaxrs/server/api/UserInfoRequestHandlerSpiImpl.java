/*
 * Copyright (C) 2016-2022 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.api;


import java.util.List;
import java.util.Map;
import com.authlete.common.assurance.VerifiedClaims;
import com.authlete.common.assurance.constraint.VerifiedClaimsConstraint;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.DatasetDao;
import com.authlete.jaxrs.server.db.UserDao;
import com.authlete.jaxrs.server.db.VerifiedClaimsDao;
import com.authlete.jaxrs.spi.UserInfoRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.UserInfoRequestHandlerSpi
 * UserInfoRequestHandlerSpi} interface which needs to be given to the
 * constructor of {@link com.authlete.jaxrs.UserInfoRequestHandler
 * UserInfoRequestHandler}.
 */
public class UserInfoRequestHandlerSpiImpl extends UserInfoRequestHandlerSpiAdapter
{
    private User mUser;


    @Override
    public void prepareUserClaims(String subject, String[] claimNames)
    {
        // Look up a user who has the subject.
        mUser = UserDao.getBySubject(subject);
    }


    @Override
    public Object getUserClaim(String claimName, String languageTag)
    {
        // If looking up a user has failed in prepareUserClaims().
        if (mUser == null)
        {
            // No claim is available.
            return null;
        }

        // Get the value of the claim.
        return mUser.getClaim(claimName, languageTag);
    }


    @Override
    public List<VerifiedClaims> getVerifiedClaims(String subject, VerifiedClaimsConstraint constraint)
    {
        // This method, getVerifiedClaims(String, VerifiedClaimsConstraint),
        // is no longer called since authlete-java-jaxrs 2.42 unless the
        // 'oldIdaFormatUsed' flag of UserInfoRequestHandler.Params is on.
        // Instead, getVerifiedClaims(String, Object) is called.

        // The third Implementer's Draft of OpenID Connect for Identity
        // Assurance 1.0 (which was published in September 2021) has introduced
        // many breaking changes. In addition, it is scheduled that the next
        // draft will introduce further breaking changes. The specification is
        // still unstable. It turned out to be inadequate to define Java classes
        // that correspond to data structures of elements under "verified_claims".
        // In that sense, the classes under com.authlete.common.assurance package
        // of the authlete-java-common library are no longer useful.
        //
        // Authlete 2.3 has implemented a different approach for ID3 and future
        // drafts of OIDC4IDA that is less susceptible to specification changes.

        return VerifiedClaimsDao.get(subject, constraint);
    }


    @Override
    public Object getVerifiedClaims(String subject, Object verifiedClaimsRequest)
    {
        // The list of available datasets of the subject.
        List<Map<String, Object>> datasets = DatasetDao.get(subject);

        // Build the content of "verified_claims" which meets conditions
        // of the request from the available datasets.
        return new VerifiedClaimsBuilder(verifiedClaimsRequest, datasets).build();
    }
}
