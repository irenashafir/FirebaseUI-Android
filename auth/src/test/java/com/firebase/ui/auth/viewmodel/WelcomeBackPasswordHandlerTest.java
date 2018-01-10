package com.firebase.ui.auth.viewmodel;

import com.firebase.ui.auth.data.model.FlowParameters;
import com.firebase.ui.auth.testhelpers.AutoCompleteTask;
import com.firebase.ui.auth.testhelpers.FakeAuthResult;
import com.firebase.ui.auth.testhelpers.TestConstants;
import com.firebase.ui.auth.testhelpers.TestHelper;
import com.firebase.ui.auth.viewmodel.email.WelcomeBackPasswordHandler;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link WelcomeBackPasswordHandler}.
 */
@RunWith(RobolectricTestRunner.class)
public class WelcomeBackPasswordHandlerTest {

    @Mock FirebaseAuth mMockAuth;
    @Mock CredentialsClient mMockCredentials;

    private WelcomeBackPasswordHandler mHandler;

    @Before
    public void setUp() {
        TestHelper.initialize();
        MockitoAnnotations.initMocks(this);

        mHandler = new WelcomeBackPasswordHandler(RuntimeEnvironment.application);

        FlowParameters testParams = TestHelper.getFlowParameters(Collections.singletonList(
                EmailAuthProvider.PROVIDER_ID));
        mHandler.initializeForTesting(testParams, mMockAuth, mMockCredentials, null);
    }

    @Test
    public void testSignIn_signsInAndSavesCredentials() {
        // Mock sign in to always succeed
        when(mMockAuth.signInWithEmailAndPassword(TestConstants.EMAIL, TestConstants.PASSWORD))
                .thenReturn(new AutoCompleteTask<>(FakeAuthResult.INSTANCE, true, null));

        // Mock smartlock save to always succeed
        when(mMockCredentials.save(any(Credential.class)))
                .thenReturn(new AutoCompleteTask<Void>(null, true, null));

        // Kick off the sign in flow
        mHandler.startSignIn(TestConstants.EMAIL, TestConstants.PASSWORD, null, null);

        // Verify that sign in is called with the right arguments
        verify(mMockAuth).signInWithEmailAndPassword(
                TestConstants.EMAIL, TestConstants.PASSWORD);

        // Verify that a matching credential is saved in SmartLock
        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(mMockCredentials).save(credentialCaptor.capture());

        Credential captured = credentialCaptor.getValue();
        assertEquals(captured.getId(), TestConstants.EMAIL);
        assertEquals(captured.getPassword(), TestConstants.PASSWORD);
    }
}
